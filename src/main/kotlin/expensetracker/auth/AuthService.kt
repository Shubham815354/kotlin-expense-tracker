package expensetracker.auth

import java.security.MessageDigest
import java.security.SecureRandom

/** Why a sign-up or login was rejected. */
sealed class AuthError(val message: String) {
    data object InvalidUsername : AuthError("Username must be 3-20 letters or digits.")
    data object WeakPassword : AuthError("Password needs at least 8 characters, including a letter and a number.")
    data object PasswordsDoNotMatch : AuthError("Passwords do not match.")
    data object AccountExists : AuthError("An account already exists. Please log in.")
    data object NoAccount : AuthError("No account yet. Please create one first.")
    data class WrongCredentials(val attemptsLeft: Int) : AuthError("Wrong username or password. Attempts left: $attemptsLeft.")
    data object LockedOut : AuthError("Too many failed attempts. Please restart the app.")
}

sealed interface AuthResult {
    data class Success(val username: String) : AuthResult
    data class Failure(val error: AuthError) : AuthResult
}

/** A stored account. Only a salted hash of the password is kept, never the password itself. */
private data class Account(val username: String, val salt: ByteArray, val passwordHash: ByteArray)

/**
 * In-memory single-user authentication for the console app.
 * Passwords are hashed with a random per-account salt (SHA-256) and compared in constant time.
 * After [maxAttempts] failed logins the service locks until the app restarts.
 */
class AuthService(
    private val maxAttempts: Int = 3,
    private val random: SecureRandom = SecureRandom(),
) {
    private var account: Account? = null
    private var failedAttempts = 0

    val hasAccount: Boolean get() = account != null
    val isLockedOut: Boolean get() = failedAttempts >= maxAttempts

    fun register(username: String, password: CharArray, confirmation: CharArray): AuthResult {
        val name = username.trim()
        return when {
            account != null -> AuthResult.Failure(AuthError.AccountExists)
            !isValidUsername(name) -> AuthResult.Failure(AuthError.InvalidUsername)
            !isStrongPassword(password) -> AuthResult.Failure(AuthError.WeakPassword)
            !password.contentEquals(confirmation) -> AuthResult.Failure(AuthError.PasswordsDoNotMatch)
            else -> {
                val salt = ByteArray(16).also { random.nextBytes(it) }
                account = Account(name, salt, hash(salt, password))
                AuthResult.Success(name)
            }
        }
    }

    fun login(username: String, password: CharArray): AuthResult {
        val stored = account ?: return AuthResult.Failure(AuthError.NoAccount)
        if (isLockedOut) return AuthResult.Failure(AuthError.LockedOut)
        val userMatches = stored.username.equals(username.trim(), ignoreCase = true)
        val passwordMatches = MessageDigest.isEqual(stored.passwordHash, hash(stored.salt, password))
        if (userMatches && passwordMatches) {
            failedAttempts = 0
            return AuthResult.Success(stored.username)
        }
        failedAttempts++
        return if (isLockedOut) AuthResult.Failure(AuthError.LockedOut)
        else AuthResult.Failure(AuthError.WrongCredentials(maxAttempts - failedAttempts))
    }

    companion object {
        private val USERNAME = Regex("^[A-Za-z0-9]{3,20}$")

        fun isValidUsername(name: String): Boolean = USERNAME.matches(name)

        fun isStrongPassword(password: CharArray): Boolean =
            password.size >= 8 && password.any { it.isLetter() } && password.any { it.isDigit() }

        private fun hash(salt: ByteArray, password: CharArray): ByteArray {
            val digest = MessageDigest.getInstance("SHA-256")
            digest.update(salt)
            digest.update(String(password).toByteArray(Charsets.UTF_8))
            return digest.digest()
        }
    }
}

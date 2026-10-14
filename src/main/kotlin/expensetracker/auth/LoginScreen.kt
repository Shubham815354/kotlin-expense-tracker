package expensetracker.auth

/**
 * Console sign-up and login flow. Returns the signed-in username, or null if the user
 * quits or is locked out. Passwords are read without echo when a real terminal is available.
 */
class LoginScreen(
    private val auth: AuthService,
    private val readLine: () -> String? = ::readlnOrNull,
    private val readSecret: (String) -> CharArray? = ::readPasswordHidden,
    private val print: (String) -> Unit = ::println,
) {
    fun run(): String? {
        if (!auth.hasAccount && !createAccount()) return null
        print("Please log in.")
        while (!auth.isLockedOut) {
            kotlin.io.print("Username: ")
            val username = readLine() ?: return null
            val password = readSecret("Password: ") ?: return null
            when (val result = auth.login(username, password)) {
                is AuthResult.Success -> {
                    print("Welcome, ${result.username}!")
                    return result.username
                }
                is AuthResult.Failure -> print(result.error.message)
            }
            password.fill(' ')
        }
        return null
    }

    private fun createAccount(): Boolean {
        print("Create your account.")
        while (true) {
            kotlin.io.print("Choose a username: ")
            val username = readLine() ?: return false
            val password = readSecret("Choose a password: ") ?: return false
            val confirmation = readSecret("Repeat the password: ") ?: return false
            val result = auth.register(username, password, confirmation)
            password.fill(' ')
            confirmation.fill(' ')
            when (result) {
                is AuthResult.Success -> {
                    print("Account created for ${result.username}.")
                    return true
                }
                is AuthResult.Failure -> print(result.error.message)
            }
        }
    }
}

/** Hidden input in a real terminal; falls back to a visible line (e.g. in an IDE run window). */
fun readPasswordHidden(prompt: String): CharArray? {
    val console = System.console()
    if (console != null) return console.readPassword(prompt)
    kotlin.io.print(prompt)
    return readlnOrNull()?.toCharArray()
}

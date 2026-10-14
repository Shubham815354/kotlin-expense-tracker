package expensetracker.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertIs
import kotlin.test.assertTrue

class AuthServiceTest {

    private val auth = AuthService(maxAttempts = 3)
    private fun pw(s: String) = s.toCharArray()

    private fun registerDefault() = auth.register("asha", pw("secret123"), pw("secret123"))

    @Test
    fun `valid account can be created and used to log in`() {
        assertIs<AuthResult.Success>(registerDefault())
        assertEquals(AuthResult.Success("asha"), auth.login("ASHA ", pw("secret123")))
    }

    @Test
    fun `invalid usernames are rejected`() {
        for (name in listOf("ab", "has space", "way-too-long-username-123", "")) {
            assertEquals(AuthResult.Failure(AuthError.InvalidUsername), auth.register(name, pw("secret123"), pw("secret123")), name)
        }
        assertFalse(auth.hasAccount)
    }

    @Test
    fun `weak passwords are rejected`() {
        for (password in listOf("short1", "lettersonly", "12345678")) {
            assertEquals(AuthResult.Failure(AuthError.WeakPassword), auth.register("asha", pw(password), pw(password)), password)
        }
    }

    @Test
    fun `mismatched confirmation is rejected`() {
        assertEquals(AuthResult.Failure(AuthError.PasswordsDoNotMatch), auth.register("asha", pw("secret123"), pw("secret124")))
    }

    @Test
    fun `only one account can be created`() {
        registerDefault()
        assertEquals(AuthResult.Failure(AuthError.AccountExists), auth.register("ravi", pw("other1234"), pw("other1234")))
    }

    @Test
    fun `login before sign-up reports no account`() {
        assertEquals(AuthResult.Failure(AuthError.NoAccount), auth.login("asha", pw("secret123")))
    }

    @Test
    fun `wrong password counts down remaining attempts`() {
        registerDefault()
        assertEquals(AuthResult.Failure(AuthError.WrongCredentials(2)), auth.login("asha", pw("wrong1234")))
        assertEquals(AuthResult.Failure(AuthError.WrongCredentials(1)), auth.login("ravi", pw("secret123")))
    }

    @Test
    fun `three failures lock the account even for the right password`() {
        registerDefault()
        repeat(2) { auth.login("asha", pw("wrong1234")) }
        assertEquals(AuthResult.Failure(AuthError.LockedOut), auth.login("asha", pw("wrong1234")))
        assertTrue(auth.isLockedOut)
        assertEquals(AuthResult.Failure(AuthError.LockedOut), auth.login("asha", pw("secret123")))
    }

    @Test
    fun `successful login resets the failure count`() {
        registerDefault()
        repeat(2) { auth.login("asha", pw("wrong1234")) }
        assertIs<AuthResult.Success>(auth.login("asha", pw("secret123")))
        assertIs<AuthResult.Failure>(auth.login("asha", pw("wrong1234")))
        assertFalse(auth.isLockedOut)
    }
}

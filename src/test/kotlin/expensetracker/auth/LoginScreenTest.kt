package expensetracker.auth

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LoginScreenTest {

    private fun screen(lines: List<String>, secrets: List<String>, output: MutableList<String>): LoginScreen {
        val input = lines.iterator()
        val secretInput = secrets.iterator()
        return LoginScreen(
            auth = AuthService(),
            readLine = { if (input.hasNext()) input.next() else null },
            readSecret = { if (secretInput.hasNext()) secretInput.next().toCharArray() else null },
            print = { output += it },
        )
    }

    @Test
    fun `sign up then log in returns the username`() {
        val out = mutableListOf<String>()
        val user = screen(listOf("asha", "asha"), listOf("secret123", "secret123", "secret123"), out).run()
        assertEquals("asha", user)
        assertTrue(out.any { it.startsWith("Welcome") })
    }

    @Test
    fun `invalid sign-up is retried with a message`() {
        val out = mutableListOf<String>()
        val user = screen(listOf("a b", "asha", "asha"), listOf("secret123", "secret123", "secret123", "secret123", "secret123"), out).run()
        assertEquals("asha", user)
        assertTrue(out.contains(AuthError.InvalidUsername.message))
    }

    @Test
    fun `three wrong passwords end the session`() {
        val out = mutableListOf<String>()
        val user = screen(listOf("asha", "asha", "asha", "asha"), listOf("secret123", "secret123", "bad11111", "bad22222", "bad33333"), out).run()
        assertNull(user)
        assertTrue(out.contains(AuthError.LockedOut.message))
    }
}

package app.alertbox.io.core.auth

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class PasswordPolicyTest {
    @Test
    fun `requires a long passphrase`() {
        assertEquals("Usa una frase de al menos 12 caracteres.", PasswordPolicy.validate("demasiado"))
        assertNull(PasswordPolicy.validate("una frase larga y unica"))
    }

    @Test
    fun `caps password size to avoid abusive input`() {
        assertEquals("La contraseña es demasiado larga.", PasswordPolicy.validate("x".repeat(129)))
        assertNull(PasswordPolicy.validate("x".repeat(128)))
    }
}

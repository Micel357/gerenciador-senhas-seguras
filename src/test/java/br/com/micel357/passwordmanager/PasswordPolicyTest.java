package br.com.micel357.passwordmanager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordPolicyTest {
    @Test
    void requiresALongMasterPassphrase() {
        assertThrows(VaultException.class, () -> PasswordPolicy.requireStrongMasterPassword("short"));
        assertDoesNotThrow(() -> PasswordPolicy.requireStrongMasterPassword("correct horse battery"));
    }

    @Test
    void reportsPasswordStrengthWithoutReturningThePassword() {
        var result = PasswordPolicy.assess("AveryLong!Password2026");
        assertTrue(result.valid());
        assertEquals("Muito forte", result.strength());
        assertTrue(result.score() <= 100);
    }
}

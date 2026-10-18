package br.com.micel357.passwordmanager;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class PasswordGeneratorTest {
    private final PasswordGenerator generator = new PasswordGenerator();

    @Test
    void generatesPasswordWithRequestedLengthAndCharacterGroups() {
        String password = generator.generate(new PasswordGenerator.Options(24, true, true, true, true,
                PasswordGenerator.DEFAULT_SYMBOLS));

        assertEquals(24, password.length());
        assertTrue(password.chars().anyMatch(Character::isUpperCase));
        assertTrue(password.chars().anyMatch(Character::isLowerCase));
        assertTrue(password.chars().anyMatch(Character::isDigit));
        assertTrue(password.chars().anyMatch(ch -> PasswordGenerator.DEFAULT_SYMBOLS.indexOf(ch) >= 0));
    }

    @Test
    void rejectsAnImpossibleGenerationRequest() {
        assertThrows(VaultException.class, () -> generator.generate(
                new PasswordGenerator.Options(5, true, true, true, true, "!")));
    }
}

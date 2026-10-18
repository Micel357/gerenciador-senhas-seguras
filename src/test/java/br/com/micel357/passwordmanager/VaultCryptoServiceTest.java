package br.com.micel357.passwordmanager;

import static org.junit.jupiter.api.Assertions.*;
import static br.com.micel357.passwordmanager.VaultModels.*;

import java.util.List;
import java.util.UUID;
import java.util.Base64;

import org.junit.jupiter.api.Test;

import tools.jackson.databind.json.JsonMapper;

class VaultCryptoServiceTest {
    private final VaultCryptoService crypto = new VaultCryptoService(JsonMapper.builder().build());

    @Test
    void encryptsAndDecryptsVaultContents() {
        byte[] salt = crypto.newSalt();
        byte[] key = crypto.deriveKey("correct horse battery", salt);
        VaultData original = new VaultData(1, List.of(new Entry(UUID.randomUUID(), "email", "person",
                "secret-value", "2026-10-18T00:00:00Z", "2026-10-18T00:00:00Z")));

        Envelope encrypted = crypto.encrypt(original, key, salt);
        assertFalse(new String(crypto.serialize(encrypted)).contains("secret-value"));
        assertEquals(original, crypto.decrypt(encrypted, key));
    }

    @Test
    void rejectsWrongKeyAndModifiedCiphertext() {
        byte[] salt = crypto.newSalt();
        byte[] key = crypto.deriveKey("correct horse battery", salt);
        Envelope encrypted = crypto.encrypt(VaultData.empty(), key, salt);
        byte[] wrongKey = crypto.deriveKey("different master password", salt);

        assertThrows(VaultException.class, () -> crypto.decrypt(encrypted, wrongKey));
        byte[] changedBytes = Base64.getDecoder().decode(encrypted.ciphertext());
        changedBytes[0] ^= 1;
        Envelope changed = new Envelope(encrypted.formatVersion(), encrypted.kdf(), encrypted.memoryKiB(),
                encrypted.iterations(), encrypted.parallelism(), encrypted.salt(), encrypted.nonce(),
                Base64.getEncoder().encodeToString(changedBytes));
        assertThrows(VaultException.class, () -> crypto.decrypt(changed, key));
    }
}

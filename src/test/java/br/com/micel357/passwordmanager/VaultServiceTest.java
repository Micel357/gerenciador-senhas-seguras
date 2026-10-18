package br.com.micel357.passwordmanager;

import static org.junit.jupiter.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import tools.jackson.databind.json.JsonMapper;

class VaultServiceTest {
    @TempDir Path temporaryDirectory;

    private VaultService service() {
        return new VaultService(new VaultCryptoService(JsonMapper.builder().build()),
                temporaryDirectory.resolve("vault.json").toString(),
                temporaryDirectory.resolve("backups").toString());
    }

    @Test
    void performsCrudSearchBackupRestoreAndLocking() throws Exception {
        VaultService vault = service();
        vault.initialize("correct horse battery");
        var created = vault.add("email", "person@example.test", "my-private-secret");
        assertEquals("email", vault.search("MAIL").getFirst().service());
        assertFalse(vault.list().toString().contains("my-private-secret"));
        assertTrue(Files.readString(temporaryDirectory.resolve("vault.json")).contains("ciphertext"));
        assertFalse(Files.readString(temporaryDirectory.resolve("vault.json")).contains("my-private-secret"));

        var backup = vault.createBackup();
        vault.updatePassword(created.id(), "updated-secret");
        assertEquals("updated-secret", vault.get(created.id()).password());
        vault.restoreBackup(backup.fileName());
        assertEquals("my-private-secret", vault.get(created.id()).password());
        vault.remove(created.id());
        assertTrue(vault.list().isEmpty());
        vault.lock();
        assertThrows(VaultException.class, vault::list);
        vault.unlock("correct horse battery");
        assertTrue(vault.list().isEmpty());
    }

    @Test
    void rejectsUnknownEntries() {
        VaultService vault = service();
        vault.initialize("correct horse battery");
        assertThrows(VaultException.class, () -> vault.get(UUID.randomUUID()));
    }
}

package br.com.micel357.passwordmanager;

import java.util.List;
import java.util.UUID;

public final class VaultModels {
    private VaultModels() { }

    public record Entry(UUID id, String service, String username, String password,
                        String createdAt, String updatedAt) { }

    public record Summary(UUID id, String service, String username,
                          String createdAt, String updatedAt) { }

    public record VaultData(int formatVersion, List<Entry> entries) {
        public VaultData {
            entries = entries == null ? List.of() : List.copyOf(entries);
        }

        public static VaultData empty() {
            return new VaultData(1, List.of());
        }
    }

    public record Envelope(int formatVersion, String kdf, int memoryKiB, int iterations,
                           int parallelism, String salt, String nonce, String ciphertext) { }

    public record Stats(int totalEntries, int uniqueServices, String oldestCreatedAt,
                        String newestUpdatedAt) { }

    public record PasswordAssessment(boolean valid, String strength, int score) { }

    public record Backup(String fileName, String createdAt) { }
}

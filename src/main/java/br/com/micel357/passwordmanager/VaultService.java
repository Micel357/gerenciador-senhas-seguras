package br.com.micel357.passwordmanager;

import static br.com.micel357.passwordmanager.VaultModels.*;

import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import jakarta.annotation.PreDestroy;

@Service
public class VaultService {
    private static final DateTimeFormatter BACKUP_NAME = DateTimeFormatter
            .ofPattern("yyyyMMdd-HHmmss-SSS'Z'").withZone(ZoneOffset.UTC);

    private final VaultCryptoService crypto;
    private final Path vaultPath;
    private final Path backupsPath;
    private VaultData data;
    private byte[] key;
    private byte[] salt;
    private long sessionGeneration;

    public VaultService(VaultCryptoService crypto,
                        @Value("${app.vault.path:./data/vault.json}") String vaultPath,
                        @Value("${app.vault.backups:./data/backups}") String backupsPath) {
        this.crypto = crypto;
        this.vaultPath = Path.of(vaultPath).toAbsolutePath().normalize();
        this.backupsPath = Path.of(backupsPath).toAbsolutePath().normalize();
    }

    public synchronized boolean isInitialized() {
        return Files.isRegularFile(vaultPath);
    }

    public synchronized boolean isUnlocked() {
        return key != null && data != null;
    }

    public synchronized long sessionGeneration() {
        return sessionGeneration;
    }

    public synchronized void initialize(String masterPassword) {
        PasswordPolicy.requireStrongMasterPassword(masterPassword);
        if (isInitialized()) {
            throw new VaultException(VaultException.Code.CONFLICT, "O cofre já foi inicializado.");
        }
        byte[] newSalt = crypto.newSalt();
        byte[] newKey = crypto.deriveKey(masterPassword, newSalt);
        VaultData empty = VaultData.empty();
        try {
            writeEnvelope(vaultPath, crypto.encrypt(empty, newKey, newSalt));
            this.key = newKey;
            this.salt = newSalt;
            this.data = empty;
            sessionGeneration++;
        } catch (RuntimeException exception) {
            Arrays.fill(newKey, (byte) 0);
            Arrays.fill(newSalt, (byte) 0);
            throw exception;
        }
    }

    public synchronized void unlock(String masterPassword) {
        if (!isInitialized()) {
            throw new VaultException(VaultException.Code.CONFLICT, "O cofre ainda não foi inicializado.");
        }
        Envelope envelope = readEnvelope(vaultPath);
        byte[] candidateSalt = crypto.salt(envelope);
        byte[] candidateKey = crypto.deriveKey(masterPassword, candidateSalt);
        try {
            VaultData decrypted = crypto.decrypt(envelope, candidateKey);
            lock();
            this.key = candidateKey;
            this.salt = candidateSalt;
            this.data = decrypted;
        } catch (RuntimeException exception) {
            Arrays.fill(candidateKey, (byte) 0);
            Arrays.fill(candidateSalt, (byte) 0);
            throw exception;
        }
    }

    public synchronized void lock() {
        if (key != null) Arrays.fill(key, (byte) 0);
        if (salt != null) Arrays.fill(salt, (byte) 0);
        key = null;
        salt = null;
        data = null;
        sessionGeneration++;
    }

    public synchronized List<Summary> list() {
        requireUnlocked();
        return data.entries().stream()
                .sorted(Comparator.comparing(Entry::service, String.CASE_INSENSITIVE_ORDER))
                .map(VaultService::summary)
                .toList();
    }

    public synchronized List<Summary> search(String query) {
        requireUnlocked();
        String needle = query == null ? "" : query.trim().toLowerCase(Locale.ROOT);
        if (needle.isEmpty()) return list();
        return data.entries().stream()
                .filter(entry -> entry.service().toLowerCase(Locale.ROOT).contains(needle)
                        || entry.username().toLowerCase(Locale.ROOT).contains(needle))
                .sorted(Comparator.comparing(Entry::service, String.CASE_INSENSITIVE_ORDER))
                .map(VaultService::summary)
                .toList();
    }

    public synchronized Entry get(UUID id) {
        requireUnlocked();
        return find(id);
    }

    public synchronized Entry add(String service, String username, String password) {
        requireUnlocked();
        requireEntryFields(service, username, password);
        String now = Instant.now().toString();
        Entry entry = new Entry(UUID.randomUUID(), service.trim(), username.trim(), password, now, now);
        List<Entry> entries = new ArrayList<>(data.entries());
        entries.add(entry);
        replaceData(entries);
        return entry;
    }

    public synchronized Entry generate(String service, String username, PasswordGenerator.Options options,
                                       PasswordGenerator generator) {
        return add(service, username, generator.generate(options));
    }

    public synchronized Entry updatePassword(UUID id, String password) {
        requireUnlocked();
        if (password == null || password.isBlank() || password.length() > 1024) {
            throw new VaultException(VaultException.Code.INVALID,
                    "A senha deve ter conteúdo e no máximo 1024 caracteres.");
        }
        Entry current = find(id);
        Entry updated = new Entry(current.id(), current.service(), current.username(), password,
                current.createdAt(), Instant.now().toString());
        List<Entry> entries = data.entries().stream()
                .map(entry -> entry.id().equals(id) ? updated : entry).toList();
        replaceData(entries);
        return updated;
    }

    public synchronized void remove(UUID id) {
        requireUnlocked();
        find(id);
        replaceData(data.entries().stream().filter(entry -> !entry.id().equals(id)).toList());
    }

    public PasswordAssessment validatePassword(String password) {
        return PasswordPolicy.assess(password);
    }

    public synchronized Stats stats() {
        requireUnlocked();
        List<Entry> entries = data.entries();
        String oldest = entries.stream().map(Entry::createdAt).min(String::compareTo).orElse(null);
        String newest = entries.stream().map(Entry::updatedAt).max(String::compareTo).orElse(null);
        int services = (int) entries.stream().map(entry -> entry.service().toLowerCase(Locale.ROOT)).distinct().count();
        return new Stats(entries.size(), services, oldest, newest);
    }

    public synchronized Backup createBackup() {
        requireUnlocked();
        try {
            Files.createDirectories(backupsPath);
            String name = "vault-" + BACKUP_NAME.format(Instant.now()) + "-"
                    + UUID.randomUUID().toString().substring(0, 8) + ".json";
            Path target = backupsPath.resolve(name);
            writeAtomically(target, Files.readAllBytes(vaultPath));
            return new Backup(name, Files.getLastModifiedTime(target).toInstant().toString());
        } catch (IOException exception) {
            throw new VaultException(VaultException.Code.CORRUPT, "Não foi possível criar o backup.", exception);
        }
    }

    public synchronized List<Backup> listBackups() {
        requireUnlocked();
        if (!Files.isDirectory(backupsPath)) return List.of();
        try (var paths = Files.list(backupsPath)) {
            return paths.filter(Files::isRegularFile)
                    .filter(path -> path.getFileName().toString().startsWith("vault-")
                            && path.getFileName().toString().endsWith(".json"))
                    .sorted(Comparator.comparing(Path::getFileName).reversed())
                    .map(path -> new Backup(path.getFileName().toString(), modifiedAt(path)))
                    .toList();
        } catch (IOException exception) {
            throw new VaultException(VaultException.Code.CORRUPT, "Não foi possível listar os backups.", exception);
        }
    }

    public synchronized void restoreBackup(String fileName) {
        requireUnlocked();
        if (fileName == null || !Path.of(fileName).getFileName().toString().equals(fileName)
                || !fileName.startsWith("vault-") || !fileName.endsWith(".json")) {
            throw new VaultException(VaultException.Code.INVALID, "Nome de backup inválido.");
        }
        Path backup = backupsPath.resolve(fileName).normalize();
        if (!backup.getParent().equals(backupsPath) || !Files.isRegularFile(backup)) {
            throw new VaultException(VaultException.Code.NOT_FOUND, "Backup não encontrado.");
        }
        try {
            byte[] bytes = Files.readAllBytes(backup);
            Envelope envelope = crypto.parse(bytes);
            VaultData restored = crypto.decrypt(envelope, key);
            writeAtomically(vaultPath, bytes);
            data = restored;
            byte[] restoredSalt = crypto.salt(envelope);
            Arrays.fill(salt, (byte) 0);
            salt = restoredSalt;
        } catch (IOException exception) {
            throw new VaultException(VaultException.Code.CORRUPT, "Não foi possível restaurar o backup.", exception);
        }
    }

    @PreDestroy
    public synchronized void close() {
        lock();
    }

    private void replaceData(List<Entry> entries) {
        VaultData next = new VaultData(1, entries);
        writeEnvelope(vaultPath, crypto.encrypt(next, key, salt));
        data = next;
    }

    private Entry find(UUID id) {
        return data.entries().stream().filter(entry -> entry.id().equals(id)).findFirst()
                .orElseThrow(() -> new VaultException(VaultException.Code.NOT_FOUND, "Entrada não encontrada."));
    }

    private void requireUnlocked() {
        if (!isUnlocked()) throw VaultException.locked();
    }

    private static Summary summary(Entry entry) {
        return new Summary(entry.id(), entry.service(), entry.username(), entry.createdAt(), entry.updatedAt());
    }

    private static void requireEntryFields(String service, String username, String password) {
        if (service == null || service.isBlank() || username == null || username.isBlank()
                || password == null || password.isBlank()) {
            throw new VaultException(VaultException.Code.INVALID, "Serviço, usuário e senha são obrigatórios.");
        }
        if (service.trim().length() > 120 || username.trim().length() > 240 || password.length() > 1024) {
            throw new VaultException(VaultException.Code.INVALID, "Um ou mais campos excedem o limite permitido.");
        }
    }

    private Envelope readEnvelope(Path path) {
        try {
            return crypto.parse(Files.readAllBytes(path));
        } catch (IOException exception) {
            throw new VaultException(VaultException.Code.CORRUPT, "Não foi possível ler o arquivo do cofre.", exception);
        }
    }

    private void writeEnvelope(Path path, Envelope envelope) {
        writeAtomically(path, crypto.serialize(envelope));
    }

    private static String modifiedAt(Path path) {
        try {
            return Files.getLastModifiedTime(path).toInstant().toString();
        } catch (IOException exception) {
            return "";
        }
    }

    private static void writeAtomically(Path target, byte[] bytes) {
        Path parent = target.toAbsolutePath().normalize().getParent();
        Path temporary = null;
        try {
            Files.createDirectories(parent);
            temporary = Files.createTempFile(parent, ".vault-", ".tmp");
            Files.write(temporary, bytes);
            try {
                Files.setPosixFilePermissions(temporary, Set.of(PosixFilePermission.OWNER_READ,
                        PosixFilePermission.OWNER_WRITE));
            } catch (UnsupportedOperationException ignored) {
                // Windows does not expose POSIX file permissions.
            }
            try {
                Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE, StandardCopyOption.REPLACE_EXISTING);
            } catch (AtomicMoveNotSupportedException ignored) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new VaultException(VaultException.Code.CORRUPT, "Não foi possível salvar o cofre com segurança.", exception);
        } finally {
            if (temporary != null) {
                try { Files.deleteIfExists(temporary); } catch (IOException ignored) { }
            }
        }
    }
}

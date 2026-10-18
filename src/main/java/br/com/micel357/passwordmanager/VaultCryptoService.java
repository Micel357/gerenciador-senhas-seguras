package br.com.micel357.passwordmanager;

import static br.com.micel357.passwordmanager.VaultModels.Envelope;
import static br.com.micel357.passwordmanager.VaultModels.VaultData;

import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.AEADBadTagException;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import org.bouncycastle.crypto.generators.Argon2BytesGenerator;
import org.bouncycastle.crypto.params.Argon2Parameters;
import org.springframework.stereotype.Service;

import tools.jackson.databind.json.JsonMapper;

@Service
public class VaultCryptoService {
    static final int MEMORY_KIB = 19 * 1024;
    static final int ITERATIONS = 2;
    static final int PARALLELISM = 1;
    private static final int KEY_BYTES = 32;
    private static final int SALT_BYTES = 16;
    private static final int NONCE_BYTES = 12;
    private static final String AAD_PREFIX = "gerenciador-senhas-seguras:v1:";

    private final JsonMapper jsonMapper;
    private final SecureRandom secureRandom = new SecureRandom();

    public VaultCryptoService(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    public byte[] newSalt() {
        byte[] salt = new byte[SALT_BYTES];
        secureRandom.nextBytes(salt);
        return salt;
    }

    public byte[] deriveKey(String masterPassword, byte[] salt) {
        if (masterPassword == null || salt == null || salt.length != SALT_BYTES) {
            throw new VaultException(VaultException.Code.INVALID, "Parâmetros de chave inválidos.");
        }
        char[] password = masterPassword.toCharArray();
        byte[] key = new byte[KEY_BYTES];
        Argon2Parameters.Builder builder = new Argon2Parameters.Builder(Argon2Parameters.ARGON2_id)
                .withVersion(Argon2Parameters.ARGON2_VERSION_13)
                .withMemoryAsKB(MEMORY_KIB)
                .withIterations(ITERATIONS)
                .withParallelism(PARALLELISM)
                .withSalt(salt);
        try {
            Argon2BytesGenerator generator = new Argon2BytesGenerator();
            generator.init(builder.build());
            generator.generateBytes(password, key);
            return key;
        } catch (RuntimeException exception) {
            Arrays.fill(key, (byte) 0);
            throw new VaultException(VaultException.Code.CORRUPT, "Não foi possível derivar a chave do cofre.", exception);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    public Envelope encrypt(VaultData data, byte[] key, byte[] salt) {
        byte[] plaintext = null;
        try {
            plaintext = jsonMapper.writeValueAsBytes(data);
            byte[] nonce = new byte[NONCE_BYTES];
            secureRandom.nextBytes(nonce);
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(aad(Base64.getEncoder().encodeToString(salt)));
            byte[] ciphertext = cipher.doFinal(plaintext);
            return new Envelope(1, "Argon2id", MEMORY_KIB, ITERATIONS, PARALLELISM,
                    Base64.getEncoder().encodeToString(salt),
                    Base64.getEncoder().encodeToString(nonce),
                    Base64.getEncoder().encodeToString(ciphertext));
        } catch (GeneralSecurityException | RuntimeException exception) {
            throw new VaultException(VaultException.Code.CORRUPT, "Não foi possível cifrar o cofre.", exception);
        } finally {
            if (plaintext != null) Arrays.fill(plaintext, (byte) 0);
        }
    }

    public VaultData decrypt(Envelope envelope, byte[] key) {
        validateEnvelope(envelope);
        byte[] plaintext = null;
        try {
            byte[] nonce = Base64.getDecoder().decode(envelope.nonce());
            byte[] salt = Base64.getDecoder().decode(envelope.salt());
            if (salt.length != SALT_BYTES) throw new IllegalArgumentException("Salt inválido.");
            byte[] ciphertext = Base64.getDecoder().decode(envelope.ciphertext());
            if (nonce.length != NONCE_BYTES) throw new IllegalArgumentException("Nonce inválido.");
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(key, "AES"), new GCMParameterSpec(128, nonce));
            cipher.updateAAD(aad(envelope.salt()));
            plaintext = cipher.doFinal(ciphertext);
            VaultData data = jsonMapper.readValue(plaintext, VaultData.class);
            if (data.formatVersion() != 1) throw new IllegalArgumentException("Versão de cofre não suportada.");
            return data;
        } catch (AEADBadTagException exception) {
            throw VaultException.invalidMasterPassword();
        } catch (GeneralSecurityException | IllegalArgumentException exception) {
            throw new VaultException(VaultException.Code.CORRUPT, "O cofre está inválido ou foi alterado.", exception);
        } catch (RuntimeException exception) {
            if (exception instanceof VaultException vaultException) throw vaultException;
            throw new VaultException(VaultException.Code.CORRUPT, "Não foi possível ler o cofre.", exception);
        } finally {
            if (plaintext != null) Arrays.fill(plaintext, (byte) 0);
        }
    }

    public byte[] serialize(Envelope envelope) {
        try {
            return jsonMapper.writeValueAsBytes(envelope);
        } catch (RuntimeException exception) {
            throw new VaultException(VaultException.Code.CORRUPT, "Não foi possível serializar o cofre.", exception);
        }
    }

    public Envelope parse(byte[] bytes) {
        try {
            Envelope envelope = jsonMapper.readValue(bytes, Envelope.class);
            validateEnvelope(envelope);
            return envelope;
        } catch (RuntimeException exception) {
            throw new VaultException(VaultException.Code.CORRUPT, "O arquivo do cofre está inválido.", exception);
        }
    }

    public byte[] salt(Envelope envelope) {
        validateEnvelope(envelope);
        try {
            byte[] salt = Base64.getDecoder().decode(envelope.salt());
            if (salt.length != SALT_BYTES) throw new IllegalArgumentException("Salt inválido.");
            return salt;
        } catch (IllegalArgumentException exception) {
            throw new VaultException(VaultException.Code.CORRUPT, "O salt do cofre está inválido.", exception);
        }
    }

    private static void validateEnvelope(Envelope envelope) {
        if (envelope == null || envelope.formatVersion() != 1 || !"Argon2id".equals(envelope.kdf())
                || envelope.memoryKiB() != MEMORY_KIB || envelope.iterations() != ITERATIONS
                || envelope.parallelism() != PARALLELISM || envelope.salt() == null
                || envelope.nonce() == null || envelope.ciphertext() == null) {
            throw new VaultException(VaultException.Code.CORRUPT, "Formato de cofre não suportado.");
        }
    }

    private static byte[] aad(String encodedSalt) {
        return (AAD_PREFIX + encodedSalt + ":" + MEMORY_KIB + ":" + ITERATIONS + ":" + PARALLELISM)
                .getBytes(StandardCharsets.UTF_8);
    }
}

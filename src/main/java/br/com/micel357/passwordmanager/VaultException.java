package br.com.micel357.passwordmanager;

public class VaultException extends RuntimeException {
    public enum Code { LOCKED, UNAUTHORIZED, NOT_FOUND, CONFLICT, INVALID, CORRUPT }

    private final Code code;

    public VaultException(Code code, String message) {
        super(message);
        this.code = code;
    }

    public VaultException(Code code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public Code code() {
        return code;
    }

    public static VaultException locked() {
        return new VaultException(Code.LOCKED, "O cofre está bloqueado.");
    }

    public static VaultException invalidMasterPassword() {
        return new VaultException(Code.UNAUTHORIZED, "Senha mestra inválida.");
    }
}

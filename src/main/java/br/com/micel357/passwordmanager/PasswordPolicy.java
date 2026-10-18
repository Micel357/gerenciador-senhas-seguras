package br.com.micel357.passwordmanager;

import static br.com.micel357.passwordmanager.VaultModels.PasswordAssessment;

import java.util.regex.Pattern;

public final class PasswordPolicy {
    public static final int MIN_MASTER_LENGTH = 12;
    private static final Pattern UPPER = Pattern.compile(".*[A-Z].*", Pattern.DOTALL);
    private static final Pattern LOWER = Pattern.compile(".*[a-z].*", Pattern.DOTALL);
    private static final Pattern DIGIT = Pattern.compile(".*[0-9].*", Pattern.DOTALL);
    private static final Pattern SYMBOL = Pattern.compile(".*[^A-Za-z0-9\\s].*", Pattern.DOTALL);

    private PasswordPolicy() { }

    public static void requireStrongMasterPassword(String password) {
        if (password == null || password.length() < MIN_MASTER_LENGTH || password.length() > 1024) {
            throw new VaultException(VaultException.Code.INVALID,
                    "A senha mestra deve ter entre 12 e 1024 caracteres.");
        }
    }

    public static PasswordAssessment assess(String password) {
        if (password == null || password.isEmpty()) {
            return new PasswordAssessment(false, "Fraca", 0);
        }
        int categories = 0;
        if (UPPER.matcher(password).matches()) categories++;
        if (LOWER.matcher(password).matches()) categories++;
        if (DIGIT.matcher(password).matches()) categories++;
        if (SYMBOL.matcher(password).matches()) categories++;

        int score = Math.min(100, password.length() * 3 + categories * 10);
        String strength = score >= 80 ? "Muito forte" : score >= 60 ? "Forte"
                : score >= 35 ? "Moderada" : "Fraca";
        boolean valid = password.length() >= 12 && categories >= 3;
        return new PasswordAssessment(valid, strength, score);
    }
}

package br.com.micel357.passwordmanager;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public final class PasswordGenerator {
    public static final String DEFAULT_SYMBOLS = "!@#$%^&*()_+-=[]{}|;:,.<>?";
    private static final String UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final String LOWER = "abcdefghijkmnopqrstuvwxyz";
    private static final String DIGITS = "23456789";
    private final SecureRandom random;

    public PasswordGenerator() {
        this(new SecureRandom());
    }

    PasswordGenerator(SecureRandom random) {
        this.random = random;
    }

    public String generate(Options options) {
        if (options == null) options = Options.defaults();
        if (options.length() < 12 || options.length() > 128) {
            throw new VaultException(VaultException.Code.INVALID,
                    "O comprimento da senha deve ficar entre 12 e 128 caracteres.");
        }
        String symbols = options.symbols() == null ? DEFAULT_SYMBOLS : options.symbols();
        List<String> groups = new ArrayList<>();
        if (options.uppercase()) groups.add(UPPER);
        if (options.lowercase()) groups.add(LOWER);
        if (options.digits()) groups.add(DIGITS);
        if (options.specialCharacters() && !symbols.isEmpty()) groups.add(symbols);
        if (groups.isEmpty()) {
            throw new VaultException(VaultException.Code.INVALID,
                    "Selecione ao menos um grupo de caracteres.");
        }
        if (options.length() < groups.size()) {
            throw new VaultException(VaultException.Code.INVALID,
                    "O comprimento é menor que a quantidade de grupos selecionados.");
        }

        List<Character> generated = new ArrayList<>(options.length());
        for (String group : groups) generated.add(group.charAt(random.nextInt(group.length())));
        String alphabet = String.join("", groups);
        while (generated.size() < options.length()) {
            generated.add(alphabet.charAt(random.nextInt(alphabet.length())));
        }
        Collections.shuffle(generated, random);
        StringBuilder result = new StringBuilder(generated.size());
        generated.forEach(result::append);
        return result.toString();
    }

    public record Options(int length, boolean uppercase, boolean lowercase,
                          boolean digits, boolean specialCharacters, String symbols) {
        public static Options defaults() {
            return new Options(20, true, true, true, true, DEFAULT_SYMBOLS);
        }
    }
}

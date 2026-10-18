package br.com.micel357.passwordmanager;

import static br.com.micel357.passwordmanager.VaultModels.*;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.Console;
import java.util.Arrays;
import java.util.UUID;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.cli.enabled", havingValue = "true", matchIfMissing = true)
public class VaultCli implements CommandLineRunner {
    private final VaultService vault;
    private final PasswordGenerator generator;
    private final ConfigurableApplicationContext context;

    public VaultCli(VaultService vault, PasswordGenerator generator, ConfigurableApplicationContext context) {
        this.vault = vault;
        this.generator = generator;
        this.context = context;
    }

    @Override
    public void run(String... args) throws Exception {
        BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
        try {
            authenticate(input);
            printHelp();
            while (true) {
                String line = readCommand(input);
                if (line == null || !execute(line.trim(), input)) break;
            }
        } catch (IOException exception) {
            System.out.println("Não foi possível ler a entrada do terminal.");
        } finally {
            vault.lock();
            context.close();
        }
    }

    private void authenticate(BufferedReader input) throws IOException {
        if (!vault.isInitialized()) {
            System.out.println("=== Configuração inicial ===");
            while (true) {
                String password = readSecret("Crie uma senha mestra (mínimo 12 caracteres): ", input);
                String confirmation = readSecret("Confirme a senha mestra: ", input);
                if (!password.equals(confirmation)) {
                    System.out.println("As senhas não coincidem.");
                    continue;
                }
                try {
                    vault.initialize(password);
                    return;
                } catch (VaultException exception) {
                    System.out.println(exception.getMessage());
                }
            }
        }
        while (true) {
            try {
                vault.unlock(readSecret("Senha mestra: ", input));
                return;
            } catch (VaultException exception) {
                if (exception.code() != VaultException.Code.UNAUTHORIZED) throw exception;
                System.out.println("Senha mestra inválida.");
            }
        }
    }

    private boolean execute(String line, BufferedReader input) throws IOException {
        if (line.isEmpty()) return true;
        String[] parts = line.split("\\s+", 3);
        String command = parts[0].toLowerCase();
        String first = parts.length > 1 ? parts[1] : "";
        String second = parts.length > 2 ? parts[2] : "";
        try {
            switch (command) {
                case "help" -> printHelp();
                case "add" -> {
                    requireArguments(first, second, "add <serviço> <usuário>");
                    Entry entry = vault.add(first, second, readSecret("Senha da conta: ", input));
                    System.out.println("Entrada adicionada: " + entry.id());
                }
                case "generate" -> {
                    requireArguments(first, second, "generate <serviço> <usuário>");
                    Entry entry = vault.generate(first, second, PasswordGenerator.Options.defaults(), generator);
                    System.out.println("Senha gerada e salva: " + entry.password());
                    System.out.println("ID: " + entry.id());
                }
                case "list" -> vault.list().forEach(this::printSummary);
                case "search" -> {
                    if (first.isBlank()) throw new IllegalArgumentException("Uso: search <termo>");
                    vault.search(line.substring(parts[0].length()).trim()).forEach(this::printSummary);
                }
                case "get" -> printEntry(vault.get(UUID.fromString(first)));
                case "update" -> {
                    Entry updated = vault.updatePassword(UUID.fromString(first), readSecret("Nova senha: ", input));
                    System.out.println("Senha atualizada para " + updated.service() + ".");
                }
                case "remove" -> {
                    if (!"s".equalsIgnoreCase(readLine("Remover esta entrada? (s/N): ", input))) {
                        System.out.println("Operação cancelada.");
                    } else {
                        vault.remove(UUID.fromString(first));
                        System.out.println("Entrada removida.");
                    }
                }
                case "validate" -> {
                    PasswordAssessment result = vault.validatePassword(readSecret("Senha para validar: ", input));
                    System.out.printf("Válida: %s | Força: %s (%d/100)%n", result.valid(), result.strength(), result.score());
                }
                case "stats" -> System.out.println(vault.stats());
                case "backup" -> System.out.println("Backup criado: " + vault.createBackup().fileName());
                case "backups" -> vault.listBackups().forEach(backup -> System.out.println(backup.fileName()));
                case "restore" -> {
                    if (first.isBlank()) throw new IllegalArgumentException("Uso: restore <arquivo>");
                    vault.restoreBackup(first);
                    System.out.println("Backup restaurado.");
                }
                case "quit", "exit" -> { return false; }
                default -> System.out.println("Comando desconhecido. Digite help.");
            }
        } catch (VaultException | IllegalArgumentException exception) {
            System.out.println(exception.getMessage());
        }
        return true;
    }

    private static void requireArguments(String first, String second, String usage) {
        if (first.isBlank() || second.isBlank()) throw new IllegalArgumentException("Uso: " + usage);
    }

    private String readSecret(String prompt, BufferedReader input) throws IOException {
        Console console = System.console();
        if (console != null) {
            char[] chars = console.readPassword("%s", prompt);
            if (chars == null) throw new IOException("Entrada encerrada.");
            try { return new String(chars); }
            finally { Arrays.fill(chars, '\0'); }
        }
        System.out.println("Aviso: este terminal não oculta a senha digitada.");
        return readLine(prompt, input);
    }

    private static String readCommand(BufferedReader input) throws IOException {
        Console console = System.console();
        if (console != null) return console.readLine("password-manager> ");
        System.out.print("password-manager> ");
        String value = input.readLine();
        return value;
    }

    private static String readLine(String prompt, BufferedReader input) throws IOException {
        Console console = System.console();
        if (console != null) {
            String value = console.readLine("%s", prompt);
            return value == null ? "" : value;
        }
        System.out.print(prompt);
        String value = input.readLine();
        return value == null ? "" : value;
    }

    private void printSummary(Summary entry) {
        System.out.printf("%s | %-20s | %-30s | %s%n", entry.id(), entry.service(), entry.username(), entry.createdAt());
    }

    private void printEntry(Entry entry) {
        System.out.println("ID: " + entry.id());
        System.out.println("Serviço: " + entry.service());
        System.out.println("Usuário: " + entry.username());
        System.out.println("Senha: " + entry.password());
        System.out.println("Criada em: " + entry.createdAt());
        System.out.println("Atualizada em: " + entry.updatedAt());
    }

    private static void printHelp() {
        System.out.println("Comandos: help, add <serviço> <usuário>, generate <serviço> <usuário>,");
        System.out.println("list, search <termo>, get <id>, update <id>, remove <id>, validate,");
        System.out.println("stats, backup, backups, restore <arquivo>, quit.");
        System.out.println("A API REST local também fica disponível em http://127.0.0.1:8080.");
    }
}

package br.com.micel357.passwordmanager;

import static br.com.micel357.passwordmanager.VaultModels.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class VaultController {
    private final VaultService vaultService;
    private final PasswordGenerator passwordGenerator;
    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository;

    public VaultController(VaultService vaultService, PasswordGenerator passwordGenerator,
                           AuthenticationManager authenticationManager,
                           SecurityContextRepository securityContextRepository) {
        this.vaultService = vaultService;
        this.passwordGenerator = passwordGenerator;
        this.authenticationManager = authenticationManager;
        this.securityContextRepository = securityContextRepository;
    }

    @GetMapping("/auth/csrf")
    public Map<String, String> csrf(CsrfToken token) {
        return Map.of("headerName", token.getHeaderName(), "token", token.getToken());
    }

    @GetMapping("/vault/status")
    public Map<String, Boolean> status() {
        return Map.of("initialized", vaultService.isInitialized(), "unlocked", vaultService.isUnlocked());
    }

    @PostMapping("/vault/initialize")
    public ResponseEntity<UnlockResponse> initialize(@Valid @RequestBody InitializeRequest request,
                                                     HttpServletRequest servletRequest,
                                                     HttpServletResponse servletResponse) {
        if (!request.masterPassword().equals(request.confirmation())) {
            throw new VaultException(VaultException.Code.INVALID, "As senhas mestras não coincidem.");
        }
        vaultService.initialize(request.masterPassword());
        establishSession(request.masterPassword(), servletRequest, servletResponse);
        return ResponseEntity.status(HttpStatus.CREATED).body(new UnlockResponse(true));
    }

    @PostMapping("/vault/unlock")
    public UnlockResponse unlock(@Valid @RequestBody MasterPasswordRequest request,
                                 HttpServletRequest servletRequest,
                                 HttpServletResponse servletResponse) {
        establishSession(request.masterPassword(), servletRequest, servletResponse);
        return new UnlockResponse(true);
    }

    @PostMapping("/vault/lock")
    public ResponseEntity<Void> lock(HttpServletRequest request) {
        vaultService.lock();
        SecurityContextHolder.clearContext();
        if (request.getSession(false) != null) request.getSession(false).invalidate();
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/entries")
    public List<Summary> entries(@RequestParam(required = false) String query) {
        return query == null ? vaultService.list() : vaultService.search(query);
    }

    @PostMapping("/entries")
    public ResponseEntity<Summary> add(@Valid @RequestBody EntryRequest request) {
        Entry created = vaultService.add(request.service(), request.username(), request.password());
        return ResponseEntity.status(HttpStatus.CREATED).body(summary(created));
    }

    @PostMapping("/entries/generate")
    public ResponseEntity<Summary> generate(@Valid @RequestBody GenerateRequest request) {
        Entry created = vaultService.generate(request.service(), request.username(), request.options(), passwordGenerator);
        return ResponseEntity.status(HttpStatus.CREATED).body(summary(created));
    }

    @GetMapping("/entries/{id}")
    public Entry get(@PathVariable UUID id) {
        return vaultService.get(id);
    }

    @PutMapping("/entries/{id}")
    public Summary update(@PathVariable UUID id, @Valid @RequestBody UpdatePasswordRequest request) {
        return summary(vaultService.updatePassword(id, request.password()));
    }

    @DeleteMapping("/entries/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {
        vaultService.remove(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/passwords/validate")
    public PasswordAssessment validate(@Valid @RequestBody PasswordValidationRequest request) {
        return vaultService.validatePassword(request.password());
    }

    @GetMapping("/stats")
    public Stats stats() {
        return vaultService.stats();
    }

    @GetMapping("/backups")
    public List<Backup> backups() {
        return vaultService.listBackups();
    }

    @PostMapping("/backups")
    public ResponseEntity<Backup> createBackup() {
        return ResponseEntity.status(HttpStatus.CREATED).body(vaultService.createBackup());
    }

    @PostMapping("/backups/{fileName}/restore")
    public ResponseEntity<Void> restore(@PathVariable String fileName) {
        vaultService.restoreBackup(fileName);
        return ResponseEntity.noContent().build();
    }

    private void establishSession(String masterPassword, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken("vault", masterPassword));
        if (request.getSession(false) != null) request.changeSessionId();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
    }

    private static Summary summary(Entry entry) {
        return new Summary(entry.id(), entry.service(), entry.username(), entry.createdAt(), entry.updatedAt());
    }

    public record UnlockResponse(boolean unlocked) { }
    public record MasterPasswordRequest(@NotBlank @Size(max = 1024) String masterPassword) { }
    public record InitializeRequest(@NotBlank @Size(min = 12, max = 1024) String masterPassword,
                                    @NotBlank @Size(max = 1024) String confirmation) { }
    public record EntryRequest(@NotBlank @Size(max = 120) String service,
                               @NotBlank @Size(max = 240) String username,
                               @NotBlank @Size(max = 1024) String password) { }
    public record UpdatePasswordRequest(@NotBlank @Size(max = 1024) String password) { }
    public record PasswordValidationRequest(@NotBlank @Size(max = 1024) String password) { }
    public record GenerateRequest(@NotBlank @Size(max = 120) String service,
                                  @NotBlank @Size(max = 240) String username,
                                  Integer length, Boolean uppercase, Boolean lowercase,
                                  Boolean digits, Boolean specialCharacters,
                                  @Size(max = 64) String symbols) {
        PasswordGenerator.Options options() {
            return new PasswordGenerator.Options(length == null ? 20 : length,
                    uppercase == null || uppercase, lowercase == null || lowercase,
                    digits == null || digits, specialCharacters == null || specialCharacters,
                    symbols == null ? PasswordGenerator.DEFAULT_SYMBOLS : symbols);
        }
    }
}

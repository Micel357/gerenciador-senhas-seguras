package br.com.micel357.passwordmanager;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, String>> handleAuthentication(AuthenticationException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("error", "Não foi possível autenticar com a senha mestra fornecida."));
    }

    @ExceptionHandler(VaultException.class)
    public ResponseEntity<Map<String, String>> handleVaultException(VaultException exception) {
        HttpStatus status = switch (exception.code()) {
            case LOCKED -> HttpStatus.LOCKED;
            case UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case NOT_FOUND -> HttpStatus.NOT_FOUND;
            case CONFLICT -> HttpStatus.CONFLICT;
            case INVALID -> HttpStatus.BAD_REQUEST;
            case CORRUPT -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        return ResponseEntity.status(status).body(Map.of("error", exception.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidation(MethodArgumentNotValidException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", "Dados de entrada inválidos."));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.badRequest().body(Map.of("error", "Identificador ou parâmetro inválido."));
    }
}

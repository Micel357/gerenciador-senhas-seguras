package br.com.micel357.passwordmanager;

import java.util.List;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

@Component
public class VaultAuthenticationProvider implements AuthenticationProvider {
    private final VaultService vaultService;

    public VaultAuthenticationProvider(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        Object credentials = authentication.getCredentials();
        if (!(credentials instanceof String masterPassword) || masterPassword.isBlank()) {
            throw new BadCredentialsException("Senha mestra inválida.");
        }
        try {
            vaultService.unlock(masterPassword);
        } catch (VaultException exception) {
            if (exception.code() == VaultException.Code.UNAUTHORIZED) {
                throw new BadCredentialsException("Senha mestra inválida.");
            }
            throw exception;
        }
        return UsernamePasswordAuthenticationToken.authenticated(
                new VaultSessionPrincipal(vaultService.sessionGeneration()), null,
                List.of(new SimpleGrantedAuthority("ROLE_VAULT")));
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class.isAssignableFrom(authentication);
    }
}

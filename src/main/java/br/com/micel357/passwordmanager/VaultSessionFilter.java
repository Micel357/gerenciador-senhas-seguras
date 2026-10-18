package br.com.micel357.passwordmanager;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.filter.OncePerRequestFilter;

/** Revoga sessões antigas quando o cofre é bloqueado ou desbloqueado novamente. */
public class VaultSessionFilter extends OncePerRequestFilter {
    private final VaultService vaultService;

    public VaultSessionFilter(VaultService vaultService) {
        this.vaultService = vaultService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated()
                && authentication.getPrincipal() instanceof VaultSessionPrincipal principal
                && principal.generation() != vaultService.sessionGeneration()) {
            SecurityContextHolder.clearContext();
            if (request.getSession(false) != null) request.getSession(false).invalidate();
        }
        chain.doFilter(request, response);
    }
}

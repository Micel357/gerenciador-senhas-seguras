package br.com.micel357.passwordmanager;

/** Identifica a geração do desbloqueio que autenticou uma sessão da API. */
public record VaultSessionPrincipal(long generation) { }

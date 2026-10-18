package br.com.micel357.passwordmanager;

import static org.junit.jupiter.api.Assertions.*;

import java.net.CookieManager;
import java.net.CookiePolicy;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;

import tools.jackson.databind.json.JsonMapper;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = {"app.cli.enabled=false",
                "app.vault.path=${java.io.tmpdir}/password-manager-api-test/vault.json",
                "app.vault.backups=${java.io.tmpdir}/password-manager-api-test/backups"})
class VaultApiIntegrationTest {
    private static final Path TEST_ROOT = Path.of(System.getProperty("java.io.tmpdir"), "password-manager-api-test");
    private static final JsonMapper JSON = JsonMapper.builder().build();

    @Autowired Environment environment;
    @Autowired VaultService vaultService;

    @BeforeAll
    static void clearTestVault() throws Exception {
        if (Files.exists(TEST_ROOT)) {
            try (var paths = Files.walk(TEST_ROOT)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) Files.deleteIfExists(path);
            }
        }
    }

    @Test
    void requiresSessionAndProtectsPasswordsInListings() throws Exception {
        vaultService.lock();
        int port = Integer.parseInt(environment.getProperty("local.server.port"));
        String base = "http://127.0.0.1:" + port;
        CookieManager cookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient client = HttpClient.newBuilder().cookieHandler(cookies).connectTimeout(Duration.ofSeconds(5)).build();

        HttpResponse<String> unauthorized = send(client, base + "/api/v1/entries", "GET", null, null);
        assertEquals(401, unauthorized.statusCode());

        HttpResponse<String> csrfResponse = send(client, base + "/api/v1/auth/csrf", "GET", null, null);
        assertEquals(200, csrfResponse.statusCode());
        String csrf = JSON.readTree(csrfResponse.body()).get("token").stringValue();

        HttpResponse<String> initialized = send(client, base + "/api/v1/vault/initialize", "POST",
                "{\"masterPassword\":\"correct horse battery\",\"confirmation\":\"correct horse battery\"}", csrf);
        assertEquals(201, initialized.statusCode());

        HttpResponse<String> invalid = send(client, base + "/api/v1/entries", "POST",
                "{\"service\":\"\",\"username\":\"user\",\"password\":\"secret\"}", csrf);
        assertEquals(400, invalid.statusCode());

        HttpResponse<String> created = send(client, base + "/api/v1/entries", "POST",
                "{\"service\":\"mail\",\"username\":\"user\",\"password\":\"private-value\"}", csrf);
        assertEquals(201, created.statusCode());
        String id = JSON.readTree(created.body()).get("id").stringValue();

        HttpResponse<String> list = send(client, base + "/api/v1/entries", "GET", null, null);
        assertEquals(200, list.statusCode());
        assertFalse(list.body().contains("private-value"));
        assertEquals("private-value", JSON.readTree(send(client, base + "/api/v1/entries/" + id,
                "GET", null, null).body()).get("password").stringValue());

        HttpResponse<String> locked = send(client, base + "/api/v1/vault/lock", "POST", "{}", csrf);
        assertEquals(204, locked.statusCode());
        assertEquals(401, send(client, base + "/api/v1/entries", "GET", null, null).statusCode());

        CookieManager secondCookies = new CookieManager(null, CookiePolicy.ACCEPT_ALL);
        HttpClient secondClient = HttpClient.newBuilder().cookieHandler(secondCookies)
                .connectTimeout(Duration.ofSeconds(5)).build();
        HttpResponse<String> secondCsrfResponse = send(secondClient, base + "/api/v1/auth/csrf", "GET", null, null);
        String secondCsrf = JSON.readTree(secondCsrfResponse.body()).get("token").stringValue();
        assertEquals(200, send(secondClient, base + "/api/v1/vault/unlock", "POST",
                "{\"masterPassword\":\"correct horse battery\"}", secondCsrf).statusCode());
        assertEquals(401, send(client, base + "/api/v1/entries", "GET", null, null).statusCode(),
                "uma sessão antiga não pode recuperar acesso após outro desbloqueio");
        assertEquals(204, send(secondClient, base + "/api/v1/vault/lock", "POST", "{}", secondCsrf).statusCode());
        assertEquals(401, send(secondClient, base + "/api/v1/entries", "GET", null, null).statusCode());
    }

    private static HttpResponse<String> send(HttpClient client, String url, String method,
                                             String body, String csrf) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url)).timeout(Duration.ofSeconds(10));
        if (csrf != null) builder.header("X-CSRF-TOKEN", csrf);
        if (body == null) builder.method(method, HttpRequest.BodyPublishers.noBody());
        else builder.header("Content-Type", "application/json").method(method, HttpRequest.BodyPublishers.ofString(body));
        return client.send(builder.build(), HttpResponse.BodyHandlers.ofString());
    }
}

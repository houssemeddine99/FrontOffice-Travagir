package frontoffice.services;

import frontoffice.config.EnvVariable;
import frontoffice.utils.SessionManager;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class ApiClient {
    private final HttpClient client = HttpClient.newHttpClient();
    private final AuthService authService = new AuthService();

    public CompletableFuture<HttpResponse<String>> sendWithRetry(String endpoint, String method, String body) {
        return sendRequest(endpoint, method, body).thenCompose(response -> {
            if (response.statusCode() == 401 && SessionManager.getInstance().getRefreshToken() != null) {
                return authService.refreshAccessToken().thenCompose(success -> {
                    if (success) {
                        return sendRequest(endpoint, method, body);
                    }
                    return CompletableFuture.completedFuture(response);
                });
            }
            return CompletableFuture.completedFuture(response);
        });
    }

    private CompletableFuture<HttpResponse<String>> sendRequest(String endpoint, String method, String body) {
        String token = SessionManager.getInstance().getAccessToken();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(EnvVariable.baseUrl + endpoint))
                .header("Content-Type", "application/json");

        if (token != null && !token.isBlank()) {
            requestBuilder.header("Authorization", "Bearer " + token);
        }

        HttpRequest.BodyPublisher bodyPublisher = (body == null || body.isEmpty())
                ? HttpRequest.BodyPublishers.noBody()
                : HttpRequest.BodyPublishers.ofString(body);

        switch (method.toUpperCase()) {
            case "GET" -> requestBuilder.GET();
            case "POST" -> requestBuilder.POST(bodyPublisher);
            case "PUT" -> requestBuilder.PUT(bodyPublisher);
            case "DELETE" -> requestBuilder.method("DELETE", bodyPublisher);
            case "PATCH" -> requestBuilder.method("PATCH", bodyPublisher);
            default -> throw new IllegalArgumentException("Unsupported HTTP method: " + method);
        }

        return client.sendAsync(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
    }
}

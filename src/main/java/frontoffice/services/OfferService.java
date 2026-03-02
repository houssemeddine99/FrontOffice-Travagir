package frontoffice.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.Offer;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class OfferService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<List<Offer>> getAllOffers() {
        return apiClient.sendWithRetry("/api/v1/offers", "GET", null)
                .thenApply(this::parseList);
    }

    public CompletableFuture<List<Offer>> searchOffers(String title) {
        String encoded = URLEncoder.encode(title == null ? "" : title, StandardCharsets.UTF_8);
        return apiClient.sendWithRetry("/api/v1/offers/search?title=" + encoded, "GET", null)
                .thenApply(this::parseList);
    }

    private List<Offer> parseList(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                return mapper.readValue(response.body(), new TypeReference<>() {
                });
            } catch (Exception e) {
                System.err.println("OfferService: failed to parse response body: " + response.body());
                e.printStackTrace();
            }
        }
        return List.of();
    }
}

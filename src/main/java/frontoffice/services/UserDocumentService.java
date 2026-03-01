package frontoffice.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import frontoffice.models.UserDocument;

import java.net.http.HttpResponse;
import java.util.concurrent.CompletableFuture;

public class UserDocumentService {
    private final ApiClient apiClient = new ApiClient();
    private final ObjectMapper mapper = new ObjectMapper().registerModule(new JavaTimeModule());

    public CompletableFuture<UserDocument> getMy() {
        return apiClient.sendWithRetry("/api/v1/user-documents/my", "GET", null)
                .thenApply(this::parseObject);
    }

    public CompletableFuture<HttpResponse<String>> save(UserDocument doc) {
        try {
            String body = mapper.writeValueAsString(doc);
            return apiClient.sendWithRetry("/api/v1/user-documents/save", "POST", body);
        } catch (Exception e) {
            return CompletableFuture.failedFuture(e);
        }
    }

    private UserDocument parseObject(HttpResponse<String> response) {
        if (response.statusCode() >= 200 && response.statusCode() < 300) {
            try {
                return mapper.readValue(response.body(), UserDocument.class);
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}

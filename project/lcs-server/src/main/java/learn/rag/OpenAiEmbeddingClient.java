package learn.rag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiEmbeddingClient implements EmbeddingClient {
    private static final String MODEL = "text-embedding-3-small";

    private final RestClient restClient;
    private final String apiKey;

    public OpenAiEmbeddingClient(RestClient.Builder restClientBuilder,
                                  @Value("${embedding.api-key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl("https://api.openai.com/v1").build();
        this.apiKey = apiKey;
    }

    @Override
    public List<Double> embed(String text) {
        EmbeddingResponse response = restClient.post()
                .uri("/embeddings")
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of("model", MODEL, "input", text))
                .retrieve()
                .body(EmbeddingResponse.class);

        if (response == null || response.data() == null || response.data().isEmpty()) {
            throw new IllegalStateException("Embeddings API returned no data.");
        }

        return response.data().get(0).embedding();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbeddingResponse(List<EmbeddingData> data) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record EmbeddingData(List<Double> embedding) {
    }
}

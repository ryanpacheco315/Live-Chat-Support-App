package learn.rag;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Component
public class OpenAiLlmClient implements LlmClient {
    private static final String MODEL = "gpt-4o-mini";

    private final RestClient restClient;
    private final String apiKey;

    public OpenAiLlmClient(RestClient.Builder restClientBuilder,
                            @Value("${llm.api-key}") String apiKey) {
        this.restClient = restClientBuilder.baseUrl("https://api.openai.com/v1").build();
        this.apiKey = apiKey;
    }

    @Override
    public String generate(String prompt) {
        ChatCompletionResponse response = restClient.post()
                .uri("/chat/completions")
                .header("Authorization", "Bearer " + apiKey)
                .body(Map.of(
                        "model", MODEL,
                        "messages", List.of(Map.of("role", "user", "content", prompt))
                ))
                .retrieve()
                .body(ChatCompletionResponse.class);

        if (response == null || response.choices() == null || response.choices().isEmpty()) {
            throw new IllegalStateException("LLM API returned no choices.");
        }

        return response.choices().get(0).message().content();
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatCompletionResponse(List<Choice> choices) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record Choice(ChatMessage message) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ChatMessage(String content) {
    }
}

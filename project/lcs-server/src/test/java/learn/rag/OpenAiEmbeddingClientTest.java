package learn.rag;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiEmbeddingClientTest {

    @Test
    void shouldEmbedText() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://api.openai.com/v1/embeddings"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess(
                        "{\"data\":[{\"embedding\":[0.1,0.2,0.3]}]}",
                        MediaType.APPLICATION_JSON));

        OpenAiEmbeddingClient client = new OpenAiEmbeddingClient(builder, "test-key");

        List<Double> result = client.embed("laptop wifi issue");

        assertEquals(List.of(0.1, 0.2, 0.3), result);
    }

    @Test
    void shouldFailWhenResponseHasNoData() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://api.openai.com/v1/embeddings"))
                .andRespond(withSuccess("{\"data\":[]}", MediaType.APPLICATION_JSON));

        OpenAiEmbeddingClient client = new OpenAiEmbeddingClient(builder, "test-key");

        assertThrows(IllegalStateException.class, () -> client.embed("laptop wifi issue"));
    }
}

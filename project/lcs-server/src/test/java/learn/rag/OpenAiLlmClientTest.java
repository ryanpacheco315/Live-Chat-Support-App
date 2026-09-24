package learn.rag;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class OpenAiLlmClientTest {

    @Test
    void shouldGenerateText() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andExpect(header("Authorization", "Bearer test-key"))
                .andRespond(withSuccess(
                        "{\"choices\":[{\"message\":{\"content\":\"Try restarting the router.\"}}]}",
                        MediaType.APPLICATION_JSON));

        OpenAiLlmClient client = new OpenAiLlmClient(builder, "test-key");

        String result = client.generate("How do I fix a wifi issue?");

        assertEquals("Try restarting the router.", result);
    }

    @Test
    void shouldFailWhenResponseHasNoChoices() {
        RestClient.Builder builder = RestClient.builder();
        MockRestServiceServer server = MockRestServiceServer.bindTo(builder).build();

        server.expect(requestTo("https://api.openai.com/v1/chat/completions"))
                .andRespond(withSuccess("{\"choices\":[]}", MediaType.APPLICATION_JSON));

        OpenAiLlmClient client = new OpenAiLlmClient(builder, "test-key");

        assertThrows(IllegalStateException.class, () -> client.generate("How do I fix a wifi issue?"));
    }
}

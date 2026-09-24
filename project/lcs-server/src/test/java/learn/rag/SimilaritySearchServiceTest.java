package learn.rag;

import learn.models.ChatEmbedding;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimilaritySearchServiceTest {

    private final SimilaritySearchService service = new SimilaritySearchService();

    @Test
    void cosineSimilarityShouldBeOneForIdenticalVectors() {
        assertEquals(1.0, service.cosineSimilarity(List.of(1.0, 2.0, 3.0), List.of(1.0, 2.0, 3.0)), 0.0001);
    }

    @Test
    void cosineSimilarityShouldBeZeroForOrthogonalVectors() {
        assertEquals(0.0, service.cosineSimilarity(List.of(1.0, 0.0), List.of(0.0, 1.0)), 0.0001);
    }

    @Test
    void cosineSimilarityShouldBeZeroForMismatchedLengths() {
        assertEquals(0.0, service.cosineSimilarity(List.of(1.0, 0.0), List.of(1.0, 0.0, 0.0)), 0.0001);
    }

    @Test
    void shouldRankClosestVectorFirst() {
        ChatEmbedding closeMatch = new ChatEmbedding(1, 1, "wifi issue", List.of(1.0, 0.0, 0.0), LocalDateTime.now());
        ChatEmbedding unrelated = new ChatEmbedding(2, 2, "printer issue", List.of(0.0, 1.0, 0.0), LocalDateTime.now());
        ChatEmbedding partialMatch = new ChatEmbedding(3, 3, "network issue", List.of(0.7, 0.7, 0.0), LocalDateTime.now());

        List<ChatEmbedding> ranked = service.findMostSimilar(
                List.of(1.0, 0.0, 0.0), List.of(unrelated, partialMatch, closeMatch), 2);

        assertEquals(2, ranked.size());
        assertEquals(closeMatch.getId(), ranked.get(0).getId());
        assertEquals(partialMatch.getId(), ranked.get(1).getId());
    }
}

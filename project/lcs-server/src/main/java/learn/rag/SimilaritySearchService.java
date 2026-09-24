package learn.rag;

import learn.models.ChatEmbedding;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class SimilaritySearchService {

    public List<ChatEmbedding> findMostSimilar(List<Double> queryEmbedding, List<ChatEmbedding> candidates, int limit) {
        return candidates.stream()
                .sorted(Comparator.comparingDouble(
                        (ChatEmbedding candidate) -> cosineSimilarity(queryEmbedding, candidate.getEmbedding())
                ).reversed())
                .limit(limit)
                .toList();
    }

    double cosineSimilarity(List<Double> a, List<Double> b) {
        if (a == null || b == null || a.size() != b.size() || a.isEmpty()) {
            return 0.0;
        }

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;
        for (int i = 0; i < a.size(); i++) {
            dot += a.get(i) * b.get(i);
            normA += a.get(i) * a.get(i);
            normB += b.get(i) * b.get(i);
        }

        if (normA == 0.0 || normB == 0.0) {
            return 0.0;
        }

        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}

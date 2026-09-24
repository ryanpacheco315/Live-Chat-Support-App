package learn.dtos;

public class EmbeddingBackfillResponse {
    private final int embedded;
    private final int failed;

    public EmbeddingBackfillResponse(int embedded, int failed) {
        this.embedded = embedded;
        this.failed = failed;
    }

    public int getEmbedded() {
        return embedded;
    }

    public int getFailed() {
        return failed;
    }
}

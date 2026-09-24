package learn.dtos;

public class SuggestedReplyResponse {
    private final String suggestion;

    public SuggestedReplyResponse(String suggestion) {
        this.suggestion = suggestion;
    }

    public String getSuggestion() {
        return suggestion;
    }
}

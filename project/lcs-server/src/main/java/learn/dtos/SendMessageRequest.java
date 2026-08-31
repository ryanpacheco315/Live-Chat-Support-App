package learn.dtos;

public class SendMessageRequest {
    private String body;

    public SendMessageRequest() {
    }

    public SendMessageRequest(String body) {
        this.body = body;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }
}

package learn.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Result<T> {
    private final ArrayList<String> messages = new ArrayList<>();
    private T payload;
    private ResultType type = ResultType.SUCCESS;

    public List<String> getErrorMessages() {
        return new ArrayList<>(messages);
    }

    public void addErrorMessage(String message, ResultType resultType) {
        messages.add(message);
        this.type = resultType;
    }

    public void addErrorMessage(String format, ResultType resultType, Object... args) {
        messages.add(String.format(format, args));
        this.type = resultType;
    }

    public boolean isSuccess() {
        return type == ResultType.SUCCESS;
    }

    public ResultType getType() {
        return type;
    }

    public T getPayload() {
        return payload;
    }

    public void setPayload(T payload) {
        this.payload = payload;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Result<?> result = (Result<?>) o;
        return Objects.equals(messages, result.messages)
                && Objects.equals(payload, result.payload)
                && type == result.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(messages, payload, type);
    }
}

package io.github.livelocation;

public class LocationError {

    private final ErrorType type;
    private final Exception exception;

    public LocationError(ErrorType type, Exception exception) {
        this.type = type;
        this.exception = exception;
    }

    public LocationError(ErrorType type) {
        this(type, null);
    }

    public ErrorType getType() {
        return type;
    }

    public Exception exception() {
        return exception;
    }
}

package exception;

public class InvalidTaskTimingException extends RuntimeException {
    public InvalidTaskTimingException(String message) {
        super(message);
    }
}
package exception;

public class OrphanSubTaskException extends RuntimeException {
    public OrphanSubTaskException(String message) {
        super(message);
    }
}
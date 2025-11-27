package hibernate_spring_bank_app.exceptions;

public class SameSenderException extends RuntimeException {
    public SameSenderException(String message) {
        super(message);
    }
}

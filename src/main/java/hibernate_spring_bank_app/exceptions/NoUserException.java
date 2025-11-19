package hibernate_spring_bank_app.exceptions;

public class NoUserException extends RuntimeException {
    public NoUserException(String message) {
        super(message);
    }
}

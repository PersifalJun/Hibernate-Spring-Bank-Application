package hibernate_spring_bank_app.exceptions;

public class NoAccountException extends RuntimeException {
    public NoAccountException(String message) {
        super(message);
    }
}

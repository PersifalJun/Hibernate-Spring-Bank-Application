package hibernate_spring_bank_app.exceptions;

public class NotEnoughAccountsException extends RuntimeException {
    public NotEnoughAccountsException(String message) {
        super(message);
    }
}

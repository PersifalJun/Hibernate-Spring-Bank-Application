package hibernate_spring_bank_app.exceptions;

public class NotDeletedAccount extends RuntimeException {
    public NotDeletedAccount(String message) {
        super(message);
    }
}

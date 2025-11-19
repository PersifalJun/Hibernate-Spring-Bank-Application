package hibernate_spring_bank_app.exceptions;

public class FirstAccountClosedException extends RuntimeException {
    public FirstAccountClosedException(String message) {
        super(message);
    }
}

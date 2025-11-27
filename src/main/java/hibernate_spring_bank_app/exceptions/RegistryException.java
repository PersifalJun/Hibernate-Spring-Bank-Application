package hibernate_spring_bank_app.exceptions;

public class RegistryException extends RuntimeException {
    public RegistryException(String message) {
        super(message);
    }
}

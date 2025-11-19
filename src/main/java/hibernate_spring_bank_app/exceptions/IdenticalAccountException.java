package hibernate_spring_bank_app.exceptions;

public class IdenticalAccountException extends RuntimeException {
    public IdenticalAccountException(String message) {
        super(message);
    }
}

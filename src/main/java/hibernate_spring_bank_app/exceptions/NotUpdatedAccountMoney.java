package hibernate_spring_bank_app.exceptions;

public class NotUpdatedAccountMoney extends RuntimeException {
    public NotUpdatedAccountMoney(String message) {
        super(message);
    }
}

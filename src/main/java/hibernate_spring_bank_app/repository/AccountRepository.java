package hibernate_spring_bank_app.repository;

import hibernate_spring_bank_app.exceptions.NotDeletedAccount;
import hibernate_spring_bank_app.exceptions.NotUpdatedAccountMoney;
import hibernate_spring_bank_app.model.account.Account;
import hibernate_spring_bank_app.provider.SessionProvider;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Optional;

@Validated
@Repository
public class AccountRepository {
    private final SessionProvider sessionProvider;

    @Autowired
    public AccountRepository(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public void save(Account account) {
        sessionProvider.getCurrentSession().persist(account);
    }

    public Optional<Account> findById(@NotNull Long accountId) {
        return Optional.ofNullable(sessionProvider.getCurrentSession().get(Account.class, accountId));
    }

    public void deleteById(@NotNull Long accountId) {
        int updated = sessionProvider.getCurrentSession()
                .createQuery("delete from Account a where a.id = :id")
                .setParameter("id", accountId)
                .executeUpdate();

        if (updated == 0) {
            throw new NotDeletedAccount("Аккаунт не был удален");
        }
    }

    public void updateAccountMoney(Account account, BigDecimal sum) {
        Session currentSession = sessionProvider.getCurrentSession();
        int updated = sessionProvider.getCurrentSession().createQuery("UPDATE Account a SET a.moneyAmount = :money " +
                        "WHERE a.id = :id")
                .setParameter("money", sum)
                .setParameter("id", account.getId())
                .executeUpdate();
        if (updated == 0) {
            throw new NotUpdatedAccountMoney("Счёт пользователя не обновлен");
        }
        currentSession.refresh(account);
    }
}
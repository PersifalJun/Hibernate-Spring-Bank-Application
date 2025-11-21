package hibernate_spring_bank_app.repository;

import hibernate_spring_bank_app.exceptions.NoAccountException;
import hibernate_spring_bank_app.model.account.Account;
import hibernate_spring_bank_app.util.SessionProvider;
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
        getCurrentSession().persist(account);
    }

    public Optional<Account> findById(@NotNull Long accountId) {
        return Optional.ofNullable(getCurrentSession().get(Account.class, accountId));
    }

    public void deleteById(@NotNull Long accountId) {
        int updated = getCurrentSession()
                .createQuery("delete from Account a where a.id = :id")
                .setParameter("id", accountId)
                .executeUpdate();

        if (updated == 0) {
            throw new NoAccountException("Аккаунт не найден для удаления");
        }

    }

    public Session getCurrentSession() {
        return sessionProvider.getCurrentSession();
    }

    public void updateAccountMoney(Account account, BigDecimal sum) {
        Session currentSession = sessionProvider.getCurrentSession();
        getCurrentSession().createQuery("UPDATE Account a SET a.moneyAmount = :money " +
                        "WHERE a.id = :id")
                .setParameter("money", sum)
                .setParameter("id", account.getId())
                .executeUpdate();
        currentSession.refresh(account);
    }
}
package hibernate_spring_bank_app.repository;

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
        Session session = sessionProvider.getCurrentSession();
        Account account = session.get(Account.class, accountId);
        session.remove(account);
    }

    public void updateAccountMoney(Account account, BigDecimal sum) {
        account.setMoneyAmount(sum);
    }
}
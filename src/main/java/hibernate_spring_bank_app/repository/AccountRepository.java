package hibernate_spring_bank_app.repository;

import hibernate_spring_bank_app.exceptions.NoAccountException;
import hibernate_spring_bank_app.model.Account;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.Optional;



@Validated
@Repository
public class AccountRepository {
    private final SessionFactory sessionFactory;

    @Autowired
    public AccountRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;

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
        return sessionFactory.getCurrentSession();
    }
}
package hibernate_spring_bank_app.repository;

import hibernate_spring_bank_app.model.User;
import hibernate_spring_bank_app.provider.SessionProvider;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Validated
@Repository
public class UserRepository {

    private final SessionProvider sessionProvider;

    @Autowired
    public UserRepository(SessionProvider sessionProvider) {
        this.sessionProvider = sessionProvider;
    }

    public void save(User user) {
        sessionProvider.getCurrentSession().persist(user);
    }

    public List<User> getUsers() {
        return sessionProvider.getCurrentSession().createQuery("from User",User.class).getResultList();
    }

    public Optional<User> findById(@NotNull Long userId) {
        return Optional.ofNullable(sessionProvider.getCurrentSession().get(User.class, userId));
    }

    public Optional<User> findByLogin(@NotBlank String login) {
        return sessionProvider.getCurrentSession()
                .createQuery("from User u WHERE u.login = :login ", User.class)
                .setParameter("login", login)
                .uniqueResultOptional();
    }
}
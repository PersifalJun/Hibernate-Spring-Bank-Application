package hibernate_spring_bank_app.repository;

import hibernate_spring_bank_app.exceptions.NoUserException;
import hibernate_spring_bank_app.model.User;
import jakarta.validation.constraints.NotNull;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.List;
import java.util.Optional;

@Validated
@Repository
public class UserRepository {

    private final SessionFactory sessionFactory;

    @Autowired
    public UserRepository(SessionFactory sessionFactory) {
        this.sessionFactory = sessionFactory;
    }

    public void save(User user) {
        getCurrentSession().persist(user);
    }

    public List<User> getUsers() {
        return getCurrentSession().createQuery("SELECT u FROM User u",User.class)
                .list();
    }

    public Optional<User> findById(@NotNull Long userId) {
        return getCurrentSession()
                .createQuery("SELECT u FROM User u WHERE u.id = :id ",User.class)
                .setParameter("id",userId)
                .uniqueResultOptional();
    }

    public void deleteById(@NotNull Long userId) {
        getCurrentSession().createQuery("DELETE FROM User u WHERE u.id = : id")
                .setParameter("id",userId);

    }


    private Session getCurrentSession(){
        return sessionFactory.getCurrentSession();
    }
}
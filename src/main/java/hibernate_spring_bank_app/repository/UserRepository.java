package hibernate_spring_bank_app.repository;


import hibernate_spring_bank_app.exceptions.RegistryException;
import hibernate_spring_bank_app.model.User;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Validated
@Repository
public class UserRepository {
    private final List<User> users;

    public UserRepository() {
        this.users = new ArrayList<>();
    }

    public User save(User user) {
        if (users.stream().filter(Objects::nonNull).anyMatch(x -> Objects.equals(x.getId(), user.getId()))) {
            throw new RegistryException("Такой пользователь уже был добавлен!");
        } else {
            users.add(user);
            return user;
        }
    }

    public List<User> getUsers() {
        return users;
    }

    public Optional<User> findById(@NotNull Long userId) {
        return users.stream().filter(Objects::nonNull).
                filter(user -> Objects.equals(user.getId(), userId)).findFirst();
    }
}
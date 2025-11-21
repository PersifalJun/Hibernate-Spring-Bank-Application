package hibernate_spring_bank_app.service;

import hibernate_spring_bank_app.exceptions.RegistryException;
import hibernate_spring_bank_app.model.account.Account;
import hibernate_spring_bank_app.model.User;
import hibernate_spring_bank_app.repository.UserRepository;
import hibernate_spring_bank_app.repository.ref.AccountRefUser;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Optional;

@Validated
@Service
public class UserService {
    private final UserRepository userRepository;
    private final AccountRefUser accountRefUser;

    @Autowired
    public UserService(UserRepository userRepository, AccountRefUser accountRefUser) {
        this.userRepository = userRepository;
        this.accountRefUser = accountRefUser;
    }
    @Transactional(readOnly = true)
    public void showAllUsers() {
        userRepository.getUsers().forEach(System.out::println);
    }

    @Transactional
    public Optional<User> createUser(@NotBlank String login) {

        isRegistered(login);
        User user = User.builder()
                .login(login)
                .accountList(new ArrayList<>())
                .build();
        Account firstAccount = accountRefUser.createFirstUserAccount(user);
        userRepository.save(user);
        accountRefUser.saveFirstUserAccount(firstAccount);
        return Optional.of(user);
    }

    private void isRegistered(@NotBlank String login) {
        userRepository.findByLogin(login)
                .ifPresent(user -> {
                    throw new RegistryException("Пользователь с таким логином уже зарегистрирован!");
                });
    }
}
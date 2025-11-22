package hibernate_spring_bank_app.service;

import hibernate_spring_bank_app.exceptions.RegistryException;
import hibernate_spring_bank_app.model.User;
import hibernate_spring_bank_app.model.account.Account;
import hibernate_spring_bank_app.repository.UserRepository;
import hibernate_spring_bank_app.repository.ref.AccountRefUser;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.Optional;

@Slf4j
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
        log.info("Запрос на вывод всех пользователей");
        userRepository.getUsers().forEach(System.out::println);
    }

    @Transactional
    public Optional<User> createUser(@NotBlank String login) {
        log.info("Создание пользователя с логином={}", login);
        isRegistered(login);
        User user = User.builder()
                .login(login)
                .accountList(new ArrayList<>())
                .build();
        Account firstAccount = accountRefUser.createFirstUserAccount(user);
        userRepository.save(user);
        accountRefUser.saveFirstUserAccount(firstAccount);
        log.info("Пользователь с логином={} успешно создан, id={}", login, user.getId());
        return Optional.of(user);
    }

    private void isRegistered(@NotBlank String login) {
        userRepository.findByLogin(login)
                .ifPresent(user -> {
                    log.warn("Попытка регистрации уже существующего логина={}", login);
                    throw new RegistryException("Пользователь с таким логином уже зарегистрирован!");
                });
    }
}

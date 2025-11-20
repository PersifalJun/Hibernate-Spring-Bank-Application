package hibernate_spring_bank_app.ref;

import hibernate_spring_bank_app.exceptions.NoUserException;
import hibernate_spring_bank_app.model.User;
import hibernate_spring_bank_app.repository.AccountRepository;
import hibernate_spring_bank_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class AccountRefUser {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    @Autowired
    public AccountRefUser(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }
    public User findUserById(Long userId){
        return userRepository.findById(userId).orElseThrow(()-> new NoUserException("Пользователь не найден."));
    }


}
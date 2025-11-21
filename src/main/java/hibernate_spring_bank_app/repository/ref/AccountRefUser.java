package hibernate_spring_bank_app.repository.ref;

import hibernate_spring_bank_app.exceptions.NoUserException;
import hibernate_spring_bank_app.model.account.Account;
import hibernate_spring_bank_app.model.User;
import hibernate_spring_bank_app.model.account.Tag;
import hibernate_spring_bank_app.repository.AccountRepository;
import hibernate_spring_bank_app.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;


@Component
public class AccountRefUser {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;
    @Value("${account.default-amount}")
    private BigDecimal moneyAmount;

    @Autowired
    public AccountRefUser(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }
    public User findUserById(Long userId){
        return userRepository.findById(userId).orElseThrow(()-> new NoUserException("Пользователь не найден."));
    }

    public Account createFirstUserAccount(User user){
        return Account.builder()
                .moneyAmount(moneyAmount)
                .user(user)
                .tag(Tag.FIRST)
                .build();

    }
    public void saveFirstUserAccount(Account account){
        accountRepository.save(account);
    }
}
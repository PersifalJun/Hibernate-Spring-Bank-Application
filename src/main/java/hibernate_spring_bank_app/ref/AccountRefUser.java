package hibernate_spring_bank_app.ref;


import hibernate_spring_bank_app.exceptions.NoUserException;
import hibernate_spring_bank_app.model.Account;
import hibernate_spring_bank_app.model.User;
import hibernate_spring_bank_app.repository.AccountRepository;
import hibernate_spring_bank_app.repository.UserRepository;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class AccountRefUser {
    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountRefUser(AccountRepository accountRepository, UserRepository userRepository) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
    }

    public User identifyUserByAccount(Account account) {
        User user = null;
        for (Map.Entry<Long, List<Account>> entry : accountRepository.getUserAccountsMap().entrySet()) {
            if (entry.getValue().contains(account)) {
                user = userRepository.findById(entry.getKey()).
                        orElseThrow(() -> new NoUserException("Не найден пользователь"));
            }
        }
        return user;
    }

    public void addNewUserToAccountsMap(User user) {
        if (!accountRepository.getUserAccountsMap().containsKey(user.getId())) {
            accountRepository.getUserAccountsMap().put(user.getId(), user.getAccountList());
        }
    }

    public Long getMaxAccountId() {
        return accountRepository.getUserAccountsMap().values().stream().
                filter(Objects::nonNull).
                flatMap(Collection::stream).
                map(Account::getId).
                filter(Objects::nonNull).
                mapToLong(Long::longValue).
                max().orElse(0L) + 1L;
    }
}
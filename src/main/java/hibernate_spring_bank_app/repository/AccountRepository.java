package hibernate_spring_bank_app.repository;


import hibernate_spring_bank_app.exceptions.NoAccountException;
import hibernate_spring_bank_app.exceptions.NoUserException;
import hibernate_spring_bank_app.model.Account;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Repository;
import org.springframework.validation.annotation.Validated;

import java.util.*;

@Validated
@Repository
public class AccountRepository {
    private final Map<Long, List<Account>> userAccountsMap;

    public AccountRepository() {
        this.userAccountsMap = new HashMap<>();
    }

    public void save(@NotNull Long userId, Account account) {
        if (Objects.isNull(account)) {
            throw new NoAccountException("Аккаунт не найден!");
        }
        else if(!userAccountsMap.containsKey(userId)){
            throw new NoUserException("Нет пользователя для добавления аккаунта!");
        }
        else {
            userAccountsMap.get(userId).add(account);
        }
    }

    public Account findById(@NotNull Long accountId) {
        return userAccountsMap.values().stream().
                filter(Objects::nonNull).
                flatMap(Collection::stream).
                filter(account -> Objects.equals(account.getId(), accountId)).findFirst().
                orElseThrow(() -> new NoAccountException("Аккаунт не найден"));
    }

    public void deleteById(@NotNull Long userId, @NotNull Long accountId) {
        List<Account> userAccounts = userAccountsMap.get(userId);
        Account accountToDelete = userAccounts.stream().
                filter(account -> Objects.equals(account.getId(), accountId)).
                findFirst().orElseThrow(() -> new NoAccountException("Аккаунт не найден"));
        userAccounts.remove(accountToDelete);
    }

    public Map<Long, List<Account>> getUserAccountsMap() {
        return userAccountsMap;
    }
}
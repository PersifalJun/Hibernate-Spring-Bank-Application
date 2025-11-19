package hibernate_spring_bank_app.service;


import hibernate_spring_bank_app.exceptions.*;
import hibernate_spring_bank_app.model.*;
import hibernate_spring_bank_app.ref.AccountRefUser;
import hibernate_spring_bank_app.repository.AccountRepository;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Transactional
@Validated
@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private final AccountRefUser accountRefUser;

    @Value("${account.transfer-commission}")
    private BigDecimal commission;
    @Value("${account.default-amount}")
    private BigDecimal moneyAmount;

    @Autowired
    public AccountService(AccountRepository accountRepository, AccountRefUser accountRefUser) {
        this.accountRepository = accountRepository;
        this.accountRefUser = accountRefUser;
    }

    public void createAccount(@NotNull Long userId) {
        if (!accountRepository.getUserAccountsMap().containsKey(userId)) {
            throw new NoUserException("Невозможно создать аккаунт для несуществующего пользователя");
        }
        Long accountId = accountRepository.getUserAccountsMap().values().stream().
                filter(Objects::nonNull).
                flatMap(Collection::stream).
                map(Account::getId).
                filter(Objects::nonNull).
                mapToLong(Long::longValue).
                max().orElse(0L) + 1L;
//        accountRepository.save(userId, new Account(accountId, userId, moneyAmount)); -> Я изменил userId на User в Account.class
    }

    public void closeAccount(@NotNull Long accountId) {
        Account accountToDelete = accountRepository.findById(accountId);
        List<Account> accounts = null;
        Long userId = null;
        for (Map.Entry<Long, List<Account>> entry : accountRepository.getUserAccountsMap().entrySet()) {
            if (entry.getValue().contains(accountToDelete)) {
                accounts = entry.getValue();
                userId = entry.getKey();
            }
        }
        @NotNull List<Account> finalAccounts = accounts;

        try {
            checkAccountListSizeEqualsOne(finalAccounts);
            checkAccountSizeIsEmpty(finalAccounts);
            checkFirstAccountCanNotBeClosed(finalAccounts, accountId);
            Account firstAccount = accountRepository.findById(finalAccounts.get(0).getId());
            firstAccount.setMoneyAmount(firstAccount.getMoneyAmount().add(accountToDelete.getMoneyAmount()));
            printAccountClosed(accountId);
            try {
                accountRepository.deleteById(userId, accountId);
            } catch (RuntimeException ex) {
                throw new NoAccountException("Не найден аккаунт для удаления");
            }

        } catch (NotEnoughAccountsException | FirstAccountClosedException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void checkAccountListSizeEqualsOne(List<Account> accounts) {
        if (accounts.size() == 1) {
            throw new NotEnoughAccountsException("У пользователя всего один счет. Он не может его закрыть");
        }
    }

    private void checkAccountSizeIsEmpty(List<Account> accounts) {
        if (accounts.isEmpty()) {
            throw new NotEnoughAccountsException("У пользователя нет счетов");
        }
    }

    private void checkFirstAccountCanNotBeClosed(List<Account> accounts, Long accountId) {
        if (accounts.get(0).getId().equals(accountId)) {
            throw new FirstAccountClosedException("Нельзя закрыть первый аккаунт пользователя");
        }
    }

    public void makeDeposit(@NotNull Long accountId,
                            @DecimalMin(value = "10.00") BigDecimal sum) {
        Account accountToMakeDeposit = accountRepository.findById(accountId);
        accountToMakeDeposit.setMoneyAmount(accountToMakeDeposit.getMoneyAmount().add(sum));
        printCurrentAmountMoney(accountToMakeDeposit);
    }

    public void transfer(@NotNull Long accountIdSender,
                         @NotNull Long accountIdRecipient,
                         @DecimalMin(value = "10.00") BigDecimal sum) {
        Account senderAccount;
        Account recipientAccount;

        try {
            senderAccount = accountRepository.findById(accountIdSender);
        } catch (RuntimeException ex) {
            throw new NoAccountException("Не найден аккаунт отправителя");
        }
        try {
            recipientAccount = accountRepository.findById(accountIdRecipient);
        } catch (RuntimeException ex) {
            throw new NoAccountException("Не найден аккаунт получателя");
        }
        User sender = accountRefUser.identifyUserByAccount(senderAccount);
        User recipient = accountRefUser.identifyUserByAccount(recipientAccount);

        try{
            checkAccountEqualsRecipientAccount(senderAccount,recipientAccount);
            checkNotEnoughMoneyToTransfer(senderAccount,sum);
            checkSenderAccountEqualsRecipient(sender,recipient);
            recipientAccount.setMoneyAmount(recipientAccount.getMoneyAmount().add(sum.subtract(commission)));
            senderAccount.setMoneyAmount(senderAccount.getMoneyAmount().subtract(sum));
            printCurrentAmountMoney(senderAccount);
        }catch(IdenticalAccountException | NotEnoughMoneyException | SameSenderException  ex){
            System.out.println(ex.getMessage());
        }
    }
    private void checkAccountEqualsRecipientAccount(Account senderAccount,Account recipientAccount){
        if (senderAccount.equals(recipientAccount)) {
            throw new IdenticalAccountException("Счета аккаунтов идентичны");
        }

    }
    private void checkNotEnoughMoneyToTransfer(Account senderAccount,BigDecimal sum){
        if (senderAccount.getMoneyAmount().compareTo(sum) < 0) {
            throw new NotEnoughMoneyException("Недостаточно средств для перевода!");
        }

    }
    private void checkSenderAccountEqualsRecipient(User sender,User recipient){
        if (sender.getId().equals(recipient.getId())) {
            throw new SameSenderException("Нельзя осуществлять перевод средств между своими счетами!");
        }

    }

    public void withdraw(@NotNull Long accountId, @DecimalMin(value = "10.00") BigDecimal sum) {
        Account accountToWithdraw = accountRepository.findById(accountId);
        if (accountToWithdraw.getMoneyAmount().compareTo(sum) < 0) {
            throw new NotEnoughMoneyException("Недостаточно средств для снятия средств!");
        } else {
            accountToWithdraw.setMoneyAmount(accountToWithdraw.getMoneyAmount().subtract(sum));
            printCurrentAmountMoney(accountToWithdraw);
        }
    }

    private void printCurrentAmountMoney(Account senderAccount) {
        System.out.println("Текущее кол-во средств для аккаунта отправителя: " + senderAccount.getMoneyAmount());
    }

    private void printAccountClosed(Long accountId) {
        System.out.println("Аккаунт закрыт c id: " + accountId + " закрыт");
    }
}
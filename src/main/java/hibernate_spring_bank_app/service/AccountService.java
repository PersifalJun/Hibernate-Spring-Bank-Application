package hibernate_spring_bank_app.service;

import hibernate_spring_bank_app.exceptions.*;
import hibernate_spring_bank_app.model.Account;
import hibernate_spring_bank_app.model.User;
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
import java.util.List;

import static java.util.Objects.isNull;

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
    public AccountService(AccountRepository accountRepository,
                          AccountRefUser accountRefUser) {
        this.accountRepository = accountRepository;
        this.accountRefUser = accountRefUser;
    }

    @Transactional
    public void createAccount(@NotNull Long userid) {

        User user = accountRefUser.findUserById(userid);
        Account account = Account.builder()
                .moneyAmount(moneyAmount)
                .user(user)
                .build();

        accountRepository.save(account);
    }

    @Transactional
    public void closeAccount(@NotNull Long accountId) {
        Account accountToClose = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoAccountException("Не найден аккаунт для удаления"));

        User userWhoCloseAccount = accountToClose.getUser();
        if (isNull(userWhoCloseAccount)) {
            throw new NoUserException("Не найден пользователь для закрытия аккаунта!");
        }

        List<Account> accountsForUserWhoCloseAccount = userWhoCloseAccount.getAccountList();

        checkAccountListSizeEqualsOne(accountsForUserWhoCloseAccount);
        checkAccountSizeIsEmpty(accountsForUserWhoCloseAccount);
        checkFirstAccountCanNotBeClosed(accountsForUserWhoCloseAccount, accountId);

        Account firstAccount = userWhoCloseAccount.getAccountList().getFirst();
        BigDecimal firstAccountMoney = firstAccount.getMoneyAmount().add(accountToClose.getMoneyAmount());
        updateAccountMoney(firstAccount, firstAccountMoney);

        printAccountClosed(accountId);
        accountRepository.deleteById(accountId);
    }

    private void checkAccountListSizeEqualsOne(List<Account> accounts) {
        if (accounts.size() == 1) {
            throw new NotEnoughAccountsException("У пользователя всего один счет. Он не может его закрыть");
        }
    }

    private void checkAccountSizeIsEmpty(List<Account> accounts) {
        if (isNull(accounts) || accounts.isEmpty()) {
            throw new NotEnoughAccountsException("У пользователя нет счетов");
        }
    }

    private void checkFirstAccountCanNotBeClosed(List<Account> accounts, Long accountId) {
        if (accounts.getFirst().getId().equals(accountId)) {
            throw new FirstAccountClosedException("Нельзя закрыть первый аккаунт пользователя");
        }
    }

    @Transactional
    public void makeDeposit(@NotNull Long accountId,
                            @DecimalMin(value = "10.00") BigDecimal sum) {

        Account accountToMakeDeposit;

        accountToMakeDeposit = accountRepository.findById(accountId)
                .orElseThrow(() -> new NoAccountException("Не найден аккаунт для внесения депозита"));


        BigDecimal sumAfterDeposit = accountToMakeDeposit.getMoneyAmount().add(sum);
        updateAccountMoney(accountToMakeDeposit, sumAfterDeposit);
        printCurrentAmountMoney(accountToMakeDeposit);
    }

    @Transactional
    public void transfer(@NotNull Long accountIdSender,
                         @NotNull Long accountIdRecipient,
                         @DecimalMin(value = "10.00") BigDecimal sum) {
        Account senderAccount;
        Account recipientAccount;


        senderAccount = accountRepository.findById(accountIdSender).
                orElseThrow(() -> new NoAccountException("Не найден аккаунт отправителя"));

        recipientAccount = accountRepository.findById(accountIdRecipient).
                orElseThrow(() -> new NoAccountException("Не найден аккаунт получателя"));


        User sender = senderAccount.getUser();
        if (isNull(sender)) {
            throw new NoUserException("Не найден пользователь-отправитель");
        }

        User recipient = recipientAccount.getUser();
        if (isNull(recipient)) {
            throw new NoUserException("Не найден пользователь-получатель");
        }

        checkAccountEqualsRecipientAccount(senderAccount, recipientAccount);
        checkNotEnoughMoneyToTransfer(senderAccount, sum);
        checkSenderAccountEqualsRecipient(sender, recipient);

        BigDecimal updatedSenderAccountMoney = senderAccount.getMoneyAmount().subtract(sum);
        updateAccountMoney(senderAccount, updatedSenderAccountMoney);

        BigDecimal updatedRecipientAccountMoney = recipientAccount.getMoneyAmount().add(sum.subtract(commission));
        updateAccountMoney(recipientAccount, updatedRecipientAccountMoney);

        printCurrentAmountMoney(senderAccount);

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

    @Transactional
    public void withdraw(@NotNull Long accountId, @DecimalMin(value = "10.00") BigDecimal sum) {
        Account accountToWithdraw = accountRepository.findById(accountId).
                orElseThrow(() -> new NoAccountException("Не найден аккаунт для снятия средств"));
        if (accountToWithdraw.getMoneyAmount().compareTo(sum) < 0) {
            throw new NotEnoughMoneyException("Недостаточно средств для снятия средств");
        } else {
            BigDecimal updatedAccountMoneyAfterWithdraw = accountToWithdraw.getMoneyAmount().subtract(sum);
            updateAccountMoney(accountToWithdraw, updatedAccountMoneyAfterWithdraw);
            printCurrentAmountMoney(accountToWithdraw);
        }
    }

    private void printCurrentAmountMoney(Account account) {
        System.out.println("Текущее кол-во средств на аккаунте id=" +
                account.getId() + ": " + account.getMoneyAmount());
    }

    private void printAccountClosed(Long accountId) {
        System.out.println("Аккаунт закрыт c id: " + accountId + " закрыт");
    }

    private void updateAccountMoney(Account account, BigDecimal sum) {
        account.setMoneyAmount(sum);
    }

}
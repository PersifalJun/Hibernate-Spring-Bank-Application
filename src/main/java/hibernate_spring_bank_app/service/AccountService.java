package hibernate_spring_bank_app.service;

import hibernate_spring_bank_app.exceptions.*;
import hibernate_spring_bank_app.model.User;
import hibernate_spring_bank_app.model.account.Account;
import hibernate_spring_bank_app.model.account.Tag;
import hibernate_spring_bank_app.repository.AccountRepository;
import hibernate_spring_bank_app.repository.ref.AccountRefUser;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.math.BigDecimal;
import java.util.List;

import static java.util.Objects.isNull;

@Slf4j
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
        log.info("Создание дополнительного аккаунта для пользователя id={}", userid);
        User user = accountRefUser.findUserById(userid);
        Account account = Account.builder()
                .moneyAmount(moneyAmount)
                .user(user)
                .tag(Tag.SECONDARY)
                .build();

        accountRepository.save(account);
        log.info("Аккаунт создан для пользователя id={}", userid);
    }

    @Transactional
    public void closeAccount(@NotNull Long accountId) {
        log.info("Закрытие аккаунта id={}", accountId);
        Account accountToClose = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Не найден аккаунт для удаления id={}", accountId);
                    return new NoAccountException("Не найден аккаунт для удаления");
                });
        checkFirstAccountCanNotBeClosed(accountToClose);
        User userWhoCloseAccount = accountToClose.getUser();
        if (isNull(userWhoCloseAccount)) {
            log.error("Не найден пользователь для закрытия аккаунта id={}", accountId);
            throw new NoUserException("Не найден пользователь для закрытия аккаунта");
        }

        List<Account> accountsForUserWhoCloseAccount = userWhoCloseAccount.getAccountList();

        checkAccountListSizeEqualsOne(accountsForUserWhoCloseAccount);
        checkAccountSizeIsEmpty(accountsForUserWhoCloseAccount);

        Account firstAccount = accountsForUserWhoCloseAccount.stream()
                .filter(account -> account.getTag() == Tag.FIRST)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("У пользователя id={} не найден первый аккаунт при закрытии аккаунта id={}",
                            userWhoCloseAccount.getId(), accountId);
                    return new NoAccountException("Аккаунт не найден");
                });

        BigDecimal firstAccountMoney = firstAccount.getMoneyAmount().add(accountToClose.getMoneyAmount());
        log.debug("Перевод средств {} с аккаунта id={} на первый аккаунт id={}",
                accountToClose.getMoneyAmount(), accountId, firstAccount.getId());
        accountRepository.updateAccountMoney(firstAccount, firstAccountMoney);

        printAccountClosed(accountId);
        accountRepository.deleteById(accountId);
        log.info("Аккаунт id={} успешно закрыт", accountId);
    }

    private void checkAccountListSizeEqualsOne(List<Account> accounts) {
        if (accounts.size() == 1) {
            log.error("Попытка закрыть аккаунт при единственном счёте у пользователя");
            throw new NotEnoughAccountsException("У пользователя всего один счет. Он не может его закрыть");
        }
    }

    private void checkAccountSizeIsEmpty(List<Account> accounts) {
        if (isNull(accounts) || accounts.isEmpty()) {
            log.error("У пользователя нет счетов при операции с аккаунтами");
            throw new NotEnoughAccountsException("У пользователя нет счетов");
        }
    }

    private void checkFirstAccountCanNotBeClosed(Account account) {
        if (account.getTag().equals(Tag.FIRST)) {
            log.error("Попытка закрыть первый аккаунт пользователя id={}", account.getUser().getId());
            throw new FirstAccountClosedException("Нельзя закрыть первый аккаунт пользователя");
        }
    }

    @Transactional
    public void makeDeposit(@NotNull Long accountId,
                            @DecimalMin(value = "10.00") BigDecimal sum) {
        log.info("Пополнение аккаунта id={} на сумму {}", accountId, sum);

        Account accountToMakeDeposit;

        accountToMakeDeposit = accountRepository.findById(accountId)
                .orElseThrow(() -> {
                    log.error("Не найден аккаунт для внесения депозита id={}", accountId);
                    return new NoAccountException("Не найден аккаунт для внесения депозита");
                });

        BigDecimal sumAfterDeposit = accountToMakeDeposit.getMoneyAmount().add(sum);
        accountRepository.updateAccountMoney(accountToMakeDeposit, sumAfterDeposit);
        log.debug("Новый баланс аккаунта id={} после пополнения: {}", accountId, sumAfterDeposit);
        printCurrentAmountMoney(accountToMakeDeposit);
    }

    @Transactional
    public void transfer(@NotNull Long accountIdSender,
                         @NotNull Long accountIdRecipient,
                         @DecimalMin(value = "10.00") BigDecimal sum) {
        log.info("Начало перевода средств: отправитель={}, получатель={}, сумма={}",
                accountIdSender, accountIdRecipient, sum);
        Account senderAccount;
        Account recipientAccount;

        senderAccount = accountRepository.findById(accountIdSender).
                orElseThrow(() -> {
                    log.error("Не найден аккаунт отправителя id={}", accountIdSender);
                    return new NoAccountException("Не найден аккаунт отправителя");
                });

        recipientAccount = accountRepository.findById(accountIdRecipient).
                orElseThrow(() -> {
                    log.error("Не найден аккаунт получателя id={}", accountIdRecipient);
                    return new NoAccountException("Не найден аккаунт получателя");
                });

        User sender = senderAccount.getUser();
        if (isNull(sender)) {
            log.error("Не найден пользователь-отправитель для аккаунта id={}", accountIdSender);
            throw new NoUserException("Не найден пользователь-отправитель");
        }
        User recipient = recipientAccount.getUser();
        if (isNull(recipient)) {
            log.error("Не найден пользователь-получатель для аккаунта id={}", accountIdRecipient);
            throw new NoUserException("Не найден пользователь-получатель");
        }

        checkAccountEqualsRecipientAccount(senderAccount, recipientAccount);
        checkNotEnoughMoneyToTransfer(senderAccount, sum);
        checkSenderAccountEqualsRecipient(sender, recipient);

        BigDecimal updatedSenderAccountMoney = senderAccount.getMoneyAmount().subtract(sum);
        accountRepository.updateAccountMoney(senderAccount, updatedSenderAccountMoney);

        BigDecimal updatedRecipientAccountMoney = recipientAccount.getMoneyAmount().add(sum.subtract(commission));
        accountRepository.updateAccountMoney(recipientAccount, updatedRecipientAccountMoney);

        log.info("Перевод завершён: отправитель={}, новый баланс отправителя={}, получатель={}, новый баланс получателя={}",
                accountIdSender, updatedSenderAccountMoney, accountIdRecipient, updatedRecipientAccountMoney);
        printCurrentAmountMoney(senderAccount);

    }
    private void checkAccountEqualsRecipientAccount(Account senderAccount,Account recipientAccount){
        if (senderAccount.equals(recipientAccount)) {
            log.warn("Попытка перевода между одинаковыми аккаунтами id={}", senderAccount.getId());
            throw new IdenticalAccountException("Счета аккаунтов идентичны");
        }
    }
    private void checkNotEnoughMoneyToTransfer(Account senderAccount,BigDecimal sum){
        if (senderAccount.getMoneyAmount().compareTo(sum) < 0) {
            log.error("Недостаточно средств для перевода: аккаунт id={}, баланс={}, запрошено={}",
                    senderAccount.getId(), senderAccount.getMoneyAmount(), sum);
            throw new NotEnoughMoneyException("Недостаточно средств для перевода!");
        }
    }
    private void checkSenderAccountEqualsRecipient(User sender,User recipient){
        if (sender.getId().equals(recipient.getId())) {
            log.warn("Попытка перевода между своими счетами: пользователь id={}", sender.getId());
            throw new SameSenderException("Нельзя осуществлять перевод средств между своими счетами!");
        }
    }

    @Transactional
    public void withdraw(@NotNull Long accountId, @DecimalMin(value = "10.00") BigDecimal sum) {
        log.info("Снятие средств со счёта id={} на сумму {}", accountId, sum);
        Account accountToWithdraw = accountRepository.findById(accountId).
                orElseThrow(() -> {
                    log.error("Не найден аккаунт для снятия средств id={}", accountId);
                    return new NoAccountException("Не найден аккаунт для снятия средств");
                });
        checkAccountMoneyNotZero(accountToWithdraw, sum);
        BigDecimal updatedAccountMoneyAfterWithdraw = accountToWithdraw.getMoneyAmount().subtract(sum);
        accountRepository.updateAccountMoney(accountToWithdraw, updatedAccountMoneyAfterWithdraw);
        log.debug("Новый баланс аккаунта id={} после снятия: {}", accountId, updatedAccountMoneyAfterWithdraw);
        printCurrentAmountMoney(accountToWithdraw);

    }

    private void checkAccountMoneyNotZero(Account account, BigDecimal sum) {
        if (account.getMoneyAmount().compareTo(sum) < 0) {
            log.error("Недостаточно средств для снятия: аккаунт id={}, баланс={}, запрошено={}",
                    account.getId(), account.getMoneyAmount(), sum);
            throw new NotEnoughMoneyException("Недостаточно средств для снятия средств");
        }
    }

    private void printCurrentAmountMoney(Account account) {
        System.out.println("Текущее кол-во средств на аккаунте id=" +
                account.getId() + ": " + account.getMoneyAmount());
    }

    private void printAccountClosed(Long accountId) {
        System.out.println("Аккаунт закрыт c id: " + accountId + " закрыт");
    }
}

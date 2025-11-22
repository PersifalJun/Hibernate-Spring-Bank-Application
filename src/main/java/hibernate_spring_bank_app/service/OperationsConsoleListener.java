package hibernate_spring_bank_app.service;

import hibernate_spring_bank_app.commands.Commands;
import hibernate_spring_bank_app.exceptions.*;
import hibernate_spring_bank_app.model.User;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Scanner;

@Slf4j
@Service
public class OperationsConsoleListener {
    private final AccountService accountService;
    private final UserService userService;
    private static final String EXIT = "EXIT";
    private final Scanner scanner = new Scanner(System.in);

    @Autowired
    public OperationsConsoleListener(AccountService accountService, UserService userService) {
        this.accountService = accountService;
        this.userService = userService;
    }

    public void start() {
        log.info("Запуск консольного слушателя операций");
        Thread consoleThread = new Thread(this::runConsole, "console-loop");
        consoleThread.start();
    }

    public void printCommands() {
        Commands[] commands = Commands.values();
        System.out.println("Выберите действие:");
        for (Commands command : commands) {
            System.out.println("- " + command);
        }
        System.out.println("- EXIT->Выход");
    }

    private void runConsole() {
        boolean running = true;
        while (running) {
            try {
                String command = scanner.nextLine().trim().toUpperCase();
                log.info("Получена команда: {}", command);
                if (command.equalsIgnoreCase(EXIT)) {
                    System.out.println("Выход");
                    log.info("Консольный слушатель остановлен по команде EXIT");
                    running = false;
                    continue;
                }
                Commands commands;
                try {
                    commands = Commands.valueOf(command);
                } catch (IllegalArgumentException ex) {
                    log.warn("Введена неизвестная команда: {}", command);
                    System.out.println("Неизвестная команда");
                    continue;
                }
                switch (commands) {
                    case USER_CREATE -> {
                        System.out.println("Создание пользователя");
                        System.out.println("Введите логин пользователя:");
                        createUser();
                    }
                    case SHOW_ALL_USERS -> {
                        System.out.println("Все пользователи:");
                        showAllUsers();
                    }
                    case ACCOUNT_CREATE -> {
                        System.out.println("Создание аккаунта:");
                        System.out.println("Введите Id пользователя:");
                        createAccount();
                    }
                    case ACCOUNT_CLOSE -> {
                        System.out.println("Введите Id аккаунта для закрытия:");
                        closeAccount();
                    }
                    case ACCOUNT_DEPOSIT -> {
                        System.out.println("Введите Id аккаунта и сумму для внесения депозита:");
                        makeDeposit();

                    }
                    case ACCOUNT_TRANSFER -> {
                        System.out.println("Введите Id счёта отправителя," +
                                "Id счёта получателя и сумму для перевода средств:");
                        transfer();
                    }
                    case ACCOUNT_WITHDRAW -> {
                        System.out.println("Введите Id счёта и сумму для снятия средств:");
                        withdraw();
                    }

                }

            } catch (NullPointerException ex) {
                log.error("NullPointerException в консольном слушателе", ex);
                System.out.println(ex.getMessage());
            }
        }
    }

    private void createUser() {
        String login = scanner.nextLine();
        log.info("Запрос на создание пользователя с логином={}", login);
        try {
            User newUser = userService.createUser(login)
                    .orElseThrow(() -> new NoUserException("Не удалось создать пользователя"));
            printUserCreated(newUser);
            log.info("Пользователь с логином={} успешно создан", login);
        } catch (ConstraintViolationException | RegistryException | NoUserException ex) {
            log.warn("Ошибка при создании пользователя с логином={}: {}", login, ex.getMessage());
            System.out.println(ex.getMessage());
        }
    }

    private void printUserCreated(User user) {
        System.out.println("Пользователь с логином: " + user.getLogin() + " создан");
    }

    private void showAllUsers() {
        log.info("Запрос на вывод всех пользователей");
        userService.showAllUsers();
    }

    private void createAccount() {
        try {
            Long userId = Long.parseLong(scanner.nextLine());
            log.info("Запрос на создание аккаунта для пользователя id={}", userId);
            accountService.createAccount(userId);
            printAccountCreated(userId);
        } catch (ConstraintViolationException | NoUserException ex) {
            log.warn("Ошибка при создании аккаунта для пользователя: {}", ex.getMessage());
            System.out.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            log.warn("Неправильный формат ввода id при создании аккаунта", ex);
            System.out.println("Неправильный формат ввода id");
        }
    }

    private void printAccountCreated(Long userId) {
        System.out.println("Аккаунт для пользователя с id: " + userId + " создан");
    }

    private void closeAccount() {
        try {
            Long accountId = Long.parseLong(scanner.nextLine());
            log.info("Запрос на закрытие аккаунта id={}", accountId);
            accountService.closeAccount(accountId);
        } catch (ConstraintViolationException |
                 NotEnoughAccountsException | NoAccountException | NoUserException |
                 FirstAccountClosedException | NotDeletedAccount | NotUpdatedAccountMoney ex) {
            log.warn("Ошибка при закрытии аккаунта: {}", ex.getMessage());
            System.out.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            log.warn("Неправильный формат ввода id при закрытии аккаунта", ex);
            System.out.println("Неправильный формат ввода id");
        }
    }

    private void makeDeposit() {
        try {
            Long id = Long.parseLong(scanner.nextLine());
            BigDecimal sum = new BigDecimal(scanner.nextLine().trim());
            log.info("Запрос на пополнение аккаунта id={} на сумму {}", id, sum);
            accountService.makeDeposit(id, sum);
        } catch (ConstraintViolationException | NoAccountException | NotUpdatedAccountMoney ex) {
            log.warn("Ошибка при пополнении аккаунта: {}", ex.getMessage());
            System.out.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            log.warn("Неправильный формат ввода id или суммы при пополнении", ex);
            System.out.println("Неправильный формат ввода id или суммы");
        }
    }

    private void transfer() {
        try {
            Long senderId = Long.parseLong(scanner.nextLine());
            Long recipientId = Long.parseLong(scanner.nextLine());
            BigDecimal sum = new BigDecimal(scanner.nextLine().trim());
            log.info("Запрос на перевод: отправитель={}, получатель={}, сумма={}",
                    senderId, recipientId, sum);
            accountService.transfer(senderId, recipientId, sum);
        } catch (ConstraintViolationException | SameSenderException |
                 NoUserException | IdenticalAccountException | NotEnoughMoneyException |
                 NoAccountException | NotUpdatedAccountMoney ex) {
            log.warn("Ошибка при переводе средств: {}", ex.getMessage());
            System.out.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            log.warn("Неправильный формат ввода id или суммы при переводе", ex);
            System.out.println("Неправильный формат ввода id или суммы");
        }
    }

    private void withdraw() {
        try {
            Long id = Long.parseLong(scanner.nextLine());
            BigDecimal sum = new BigDecimal(scanner.nextLine().trim());
            log.info("Запрос на снятие средств: аккаунт id={}, сумма={}", id, sum);
            accountService.withdraw(id, sum);
        } catch (ConstraintViolationException | NoAccountException | NotUpdatedAccountMoney |
                 NotEnoughMoneyException ex) {
            log.warn("Ошибка при снятии средств: {}", ex.getMessage());
            System.out.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            log.warn("Неправильный формат ввода id или суммы при снятии средств", ex);
            System.out.println("Неправильный формат ввода id или суммы");
        }
    }
}

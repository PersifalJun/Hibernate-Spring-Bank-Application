package hibernate_spring_bank_app.service;

import hibernate_spring_bank_app.commands.Commands;
import hibernate_spring_bank_app.exceptions.*;
import hibernate_spring_bank_app.model.User;
import hibernate_spring_bank_app.repository.ref.AccountRefUser;
import jakarta.validation.ConstraintViolationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Scanner;

@Service
public class OperationsConsoleListener {
    private final AccountService accountService;
    private final UserService userService;
    private final AccountRefUser accountRefUser;
    private static final String EXIT = "EXIT";
    private final Scanner scanner = new Scanner(System.in);

    @Autowired
    public OperationsConsoleListener(AccountService accountService, UserService userService, AccountRefUser accountRefUser) {
        this.accountService = accountService;
        this.userService = userService;
        this.accountRefUser = accountRefUser;
    }

    public void start() {
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
                if (command.equalsIgnoreCase(EXIT)) {
                    System.out.println("Выход");
                    running = false;
                    continue;
                }
                Commands commands;
                try {
                    commands = Commands.valueOf(command);
                } catch (IllegalArgumentException ex) {
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
                System.out.println(ex.getMessage());
            }
        }
    }

    private void createUser() {
        String login = scanner.nextLine();
        try {
            User newUser = userService.createUser(login)
                    .orElseThrow(() -> new NoUserException("Не удалось создать пользователя"));
            printUserCreated(newUser);
        } catch (ConstraintViolationException | RegistryException | NoUserException ex) {
            System.out.println(ex.getMessage());
        }
    }

    private void printUserCreated(User user) {
        System.out.println("Пользователь с логином: " + user.getLogin() + " создан");
    }

    private void showAllUsers() {
        userService.showAllUsers();
    }

    private void createAccount() {
        try {
            Long userId = Long.parseLong(scanner.nextLine());
            accountService.createAccount(userId);
            printAccountCreated(userId);
        } catch (ConstraintViolationException | NoUserException | NoAccountException ex) {
            System.out.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            System.out.println("Неправильный формат ввода id");
        }
    }

    private void printAccountCreated(Long userId) {
        System.out.println("Аккаунт для пользователя с id: " + userId + " создан");
    }

    private void closeAccount() {
        try {
            Long accountId = Long.parseLong(scanner.nextLine());
            accountService.closeAccount(accountId);
        } catch (ConstraintViolationException |
                 NotEnoughAccountsException | NoAccountException | NoUserException |
                 FirstAccountClosedException ex) {
            System.out.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            System.out.println("Неправильный формат ввода id");
        }
    }

    private void makeDeposit() {
        try {
            Long id = Long.parseLong(scanner.nextLine());
            BigDecimal sum = new BigDecimal(scanner.nextLine().trim());
            accountService.makeDeposit(id, sum);
        } catch (ConstraintViolationException | NoAccountException ex) {
            System.out.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            System.out.println("Неправильный формат ввода id или суммы");
        }
    }

    private void transfer() {
        try {
            Long senderId = Long.parseLong(scanner.nextLine());
            Long recipientId = Long.parseLong(scanner.nextLine());
            BigDecimal sum = new BigDecimal(scanner.nextLine().trim());
            accountService.transfer(senderId, recipientId, sum);
        } catch (ConstraintViolationException | SameSenderException |
                 NotEnoughAccountsException | NoAccountException ex) {
            System.out.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            System.out.println("Неправильный формат ввода id или суммы");
        }
    }

    private void withdraw() {
        try {
            Long id = Long.parseLong(scanner.nextLine());
            BigDecimal sum = new BigDecimal(scanner.nextLine().trim());
            accountService.withdraw(id, sum);
        } catch (ConstraintViolationException | NoAccountException |
                 NotEnoughMoneyException ex) {
            System.out.println(ex.getMessage());
        } catch (NumberFormatException ex) {
            System.out.println("Неправильный формат ввода id или суммы");
        }
    }
}
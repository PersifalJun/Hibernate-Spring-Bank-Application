package hibernate_spring_bank_app;

import hibernate_spring_bank_app.service.OperationsConsoleListener;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext("hibernate_spring_bank_app");
        OperationsConsoleListener listener = context.getBean(OperationsConsoleListener.class);
        listener.printCommands();
        listener.start();
    }
}

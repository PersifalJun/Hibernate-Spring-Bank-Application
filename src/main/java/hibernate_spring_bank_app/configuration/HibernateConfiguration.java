package hibernate_spring_bank_app.configuration;

import hibernate_spring_bank_app.model.Account;
import hibernate_spring_bank_app.model.User;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@ComponentScan("hibernate_spring_bank_app")
@PropertySource("classpath:db.properties")
public class HibernateConfiguration {

    @Value("${DB_DRIVER}")
    private String dbDriver;

    @Value("${DB_URL}")
    private String dbUrl;

    @Value("${DB_USERNAME}")
    private String dbUsername;

    @Value("${DB_PASSWORD}")
    private String dbPassword;

    @Bean
    public SessionFactory sessionFactory() {
        org.hibernate.cfg.Configuration configuration = new org.hibernate.cfg.Configuration();

        configuration
                .addAnnotatedClass(User.class)
                .addAnnotatedClass(Account.class)
                .addPackage("hibernate_spring_bank_app")
                .setProperty("hibernate.connection.driver_class", dbDriver)
                .setProperty("hibernate.connection.url", dbUrl)
                .setProperty("hibernate.connection.username", dbUsername)
                .setProperty("hibernate.connection.password", dbPassword)
                .setProperty("hibernate.show_sql", "true")
                .setProperty("hibernate.format_sql", "true")
                .setProperty("hibernate.hbm2ddl.auto", "update")
                .setProperty("hibernate.current_session_context_class", "org.springframework.orm.hibernate5.SpringSessionContext");

        return configuration.buildSessionFactory();
    }
}

package hibernate_spring_bank_app.model;

import hibernate_spring_bank_app.model.account.Account;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "users",schema = "hibernate-spring-bank")
@Entity
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Логин пользователя не может быть пустым")
    @Column(name = "login", length = 20, nullable = false)
    @Size(min = 1, max = 20, message = "В логине должно быть от 1 до 20 символов" )
    private String login;

    @NotNull(message = "Аккаунты пользователя не могут быть null")
    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private List<Account> accountList;

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", login='" + login + '\'' +
                ", accountsCount=" + (accountList != null ? accountList.size() : 0) +
                '}';
    }
}
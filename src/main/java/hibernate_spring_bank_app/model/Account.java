package hibernate_spring_bank_app.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@EqualsAndHashCode(of = "id")
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "accounts",schema = "hibernate-spring-bank")
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @NotNull(message = "ID аккаунта не может быть null!")
    private Long id;

    @NotNull(message = "Аккаунт должен быть привязан к пользователю!")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @DecimalMin(value = "0.00")
    private BigDecimal moneyAmount;

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", userId=" + user+
                ", moneyAmount=" + moneyAmount +
                '}';
    }
}
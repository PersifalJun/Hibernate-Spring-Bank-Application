package hibernate_spring_bank_app.model.account;

import hibernate_spring_bank_app.model.User;
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
@Builder
@Table(name = "accounts",schema = "hibernate-spring-bank")
@Entity
public class Account {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Аккаунт должен быть привязан к пользователю!")
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @DecimalMin(value = "0.00")
    @Column(name = "money_amount")
    private BigDecimal moneyAmount;

    @Enumerated(EnumType.STRING)
    @Column(name = "tag")
    private Tag tag;

    @Override
    public String toString() {
        return "Account{" +
                "id=" + id +
                ", userId=" + (user != null ? user.getId() : null) +
                ", moneyAmount=" + moneyAmount +
                '}';
    }
}
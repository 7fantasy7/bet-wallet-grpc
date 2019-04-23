package by.botyanov.wallet.server.domain;

import by.botyanov.wallet.server.model.Currency;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLInsert;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Data
@NoArgsConstructor
@Entity
@Table(name = "wallet")
@SQLInsert(sql = "INSERT INTO wallet(amount, currency, user_id) VALUES (?, ?, ?) " +
        "ON CONFLICT (user_id, currency) DO UPDATE SET amount = wallet.amount + EXCLUDED.amount")
public class Wallet implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    // Todo maybe should better use BigDecimal, but need an approach to send via gRPC.
    // smth like: https://github.com/googleapis/googleapis/blob/master/google/type/money.proto
    @Column(name = "amount")
    private double amount;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

}

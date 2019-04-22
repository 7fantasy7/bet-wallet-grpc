package by.botyanov.wallet.server.domain;

import by.botyanov.wallet.server.model.Currency;
import lombok.Data;
import lombok.NoArgsConstructor;

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
public class Wallet implements Serializable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "amount")
    private double amount;

    @Column(name = "currency")
    @Enumerated(EnumType.STRING)
    private Currency currency;

}

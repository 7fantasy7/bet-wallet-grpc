package by.botyanov.wallet.client.event.impl;

import by.botyanov.wallet.server.model.Currency;
import by.botyanov.wallet.server.model.Deposit;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class DepositEvent implements Event<Deposit> {

    private final double amount;
    private final Currency currency;

    @Override
    public Deposit toRequest(Long userId) {
        return Deposit.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .build();
    }

}

package by.botyanov.wallet.client.event.impl;

import by.botyanov.wallet.server.model.Currency;
import by.botyanov.wallet.server.model.Withdraw;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

@ToString
@RequiredArgsConstructor
public class WithdrawEvent implements Event<Withdraw> {

    private final double amount;
    private final Currency currency;

    @Override
    public Withdraw toRequest(Long userId) {
        return Withdraw.newBuilder()
                .setUserId(userId)
                .setAmount(amount)
                .setCurrency(currency)
                .build();
    }

}

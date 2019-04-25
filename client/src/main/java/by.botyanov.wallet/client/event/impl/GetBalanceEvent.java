package by.botyanov.wallet.client.event.impl;

import by.botyanov.wallet.server.model.Balance;
import lombok.ToString;

@ToString
public class GetBalanceEvent implements Event<Balance> {

    @Override
    public Balance toRequest(Long userId) {
        return Balance.newBuilder()
                .setUserId(userId)
                .build();
    }

}

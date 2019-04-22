package by.botyanov.wallet.client;

import by.botyanov.wallet.client.event.impl.DepositEvent;
import by.botyanov.wallet.client.event.impl.Event;
import by.botyanov.wallet.client.event.impl.GetBalanceEvent;
import by.botyanov.wallet.client.event.impl.WithdrawEvent;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import static by.botyanov.wallet.server.model.Currency.EUR;
import static by.botyanov.wallet.server.model.Currency.GBP;
import static by.botyanov.wallet.server.model.Currency.USD;
import static com.google.common.collect.Lists.newArrayList;
import static java.util.Collections.unmodifiableList;

@RequiredArgsConstructor
public enum Round {

    A(unmodifiableList(newArrayList(
            new DepositEvent(100, USD),
            new WithdrawEvent(200, USD),
            new DepositEvent(100, EUR),
            new GetBalanceEvent(),
            new WithdrawEvent(100, USD),
            new GetBalanceEvent(),
            new WithdrawEvent(100, USD)
    ))),
    B(unmodifiableList(newArrayList(
            new WithdrawEvent(100, GBP),
            new DepositEvent(300, GBP),
            new WithdrawEvent(100, GBP),
            new WithdrawEvent(100, GBP),
            new WithdrawEvent(100, GBP)
    ))),
    C(unmodifiableList(newArrayList(
            new GetBalanceEvent(),
            new DepositEvent(100, USD),
            new DepositEvent(100, USD),
            new WithdrawEvent(100, USD),
            new DepositEvent(100, USD),
            new GetBalanceEvent(),
            new WithdrawEvent(200, USD),
            new GetBalanceEvent()
    )));

    @Getter
    private final List<Event> events;

    public static Round random() {
        final Round[] values = values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

}

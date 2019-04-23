package by.botyanov.wallet.client;

import by.botyanov.wallet.client.event.impl.DepositEvent;
import by.botyanov.wallet.client.event.impl.GetBalanceEvent;
import by.botyanov.wallet.client.event.impl.WithdrawEvent;
import by.botyanov.wallet.server.model.WalletServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;

import java.util.concurrent.Callable;

@RequiredArgsConstructor
public class ClientEmulator implements Callable<Void> {

    private final long userId;
    private final WalletServiceGrpc.WalletServiceBlockingStub walletService;
    private final int rounds;

    @Override
    public Void call() {
        for (int i = 0; i < rounds; i++) {
            //todo log + thread id
            final Round round = Round.random();

            round.getEvents().forEach(event -> {
                final Object request = event.toRequest(userId);
                try {
                    if (event instanceof DepositEvent) {
                        walletService.deposit((by.botyanov.wallet.server.model.Deposit) request);
                    } else if (event instanceof GetBalanceEvent) {
                        walletService.balance((by.botyanov.wallet.server.model.Balance) request);
                    } else if (event instanceof WithdrawEvent) {
                        walletService.withdraw((by.botyanov.wallet.server.model.Withdraw) request);
                    }
                    System.out.println("[" + Thread.currentThread().getName() + "]: success :D");
                } catch (StatusRuntimeException e) {
                    System.out.println(e.getStatus().getCode() + ":" + e.getStatus().getDescription());
                }
            });

        }

        System.out.println("Client finish on:" + System.nanoTime());

        return null;
    }


}

package by.botyanov.wallet.client;

import by.botyanov.wallet.client.event.impl.DepositEvent;
import by.botyanov.wallet.client.event.impl.GetBalanceEvent;
import by.botyanov.wallet.client.event.impl.WithdrawEvent;
import by.botyanov.wallet.server.model.WalletServiceGrpc;
import io.grpc.StatusRuntimeException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

@Slf4j
@RequiredArgsConstructor
public class ClientEmulator implements Callable<Void> {

    private final long userId;
    private final WalletServiceGrpc.WalletServiceBlockingStub walletService;
    private final int rounds;

    @Override
    public Void call() {
        for (int i = 0; i < rounds; i++) {
            log.debug("Starting round {} of {} for user[id={}] in thread {}",
                    i, rounds, userId, Thread.currentThread().getName());
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
                } catch (StatusRuntimeException e) {
                    log.error("Client failed request {} for user[id={}] with exception {}", event, userId, e.getStatus());
                }
            });
        }

        log.info("Client {} finished for user[id={}] on {}", Thread.currentThread().getName(), userId, System.nanoTime());

        return null;
    }


}

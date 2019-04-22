package by.botyanov.wallet.client;

import by.botyanov.wallet.client.event.impl.DepositEvent;
import by.botyanov.wallet.client.event.impl.GetBalanceEvent;
import by.botyanov.wallet.client.event.impl.WithdrawEvent;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class ClientApplication implements ApplicationRunner {

    private static final String USERS_OPT = "users";
    private static final String CONCURRENT_THREADS_PER_USER_OPT = "concurrent_threads_per_user";
    private static final String ROUNDS_PER_THREAD = "rounds_per_thread";

    public static void main(String... args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    private ManagedChannel managedChannel;
    private by.botyanov.wallet.server.model.WalletServiceGrpc.WalletServiceBlockingStub walletService;

    @PostConstruct
    private void init() {
        managedChannel = ManagedChannelBuilder.forAddress("localhost", 6565).usePlaintext().build();

        walletService = by.botyanov.wallet.server.model.WalletServiceGrpc.newBlockingStub(managedChannel);
    }

    @PreDestroy
    public void shutdown() throws InterruptedException {
        managedChannel.shutdown();
        managedChannel.awaitTermination(10, TimeUnit.SECONDS);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
//        if (!args.containsOption(USERS_OPT) || !args.containsOption(CONCURRENT_THREADS_PER_USER_OPT)
//                || !args.containsOption(ROUNDS_PER_THREAD)) {
//            throw new Exception(USERS_OPT + ", " + CONCURRENT_THREADS_PER_USER_OPT + ", " + ROUNDS_PER_THREAD
//                    + " options are required to run");
//        }
//
//        final Integer usersCount = Integer.valueOf(args.getOptionValues(USERS_OPT).get(0));
//        final Integer concurrentThreads = Integer.valueOf(args.getOptionValues(CONCURRENT_THREADS_PER_USER_OPT).get(0));
//        final Integer roundsPerThread = Integer.valueOf(args.getOptionValues(ROUNDS_PER_THREAD).get(0));

        final Integer usersCount = 3;
        final Integer concurrentThreads = 3;
        final Integer roundsPerThread = 3;

        // todo :D
        for (long userId = 0; userId < usersCount; userId++) {
            for (int thread = 0; thread < concurrentThreads; thread++) {
                for (int roundNumber = 0; roundNumber < roundsPerThread; roundNumber++) {
                    final Round round = Round.random();
                    long finalUserId = userId;
                    round.getEvents().forEach(event -> {
                        final Object request = event.toRequest(finalUserId);
                        try {
                            if (event instanceof DepositEvent) {
                                walletService.deposit((by.botyanov.wallet.server.model.Deposit) request);
                            } else if (event instanceof GetBalanceEvent) {
                                walletService.balance((by.botyanov.wallet.server.model.Balance) request);
                            } else if (event instanceof WithdrawEvent) {
                                walletService.withdraw((by.botyanov.wallet.server.model.Withdraw) request);
                            }
                            System.out.println("success :D");
                        } catch (StatusRuntimeException e) {
                            System.out.println(e.getStatus().getCode() + ":" + e.getStatus().getDescription());
                        }
                    });
                }
            }
        }
    }

}

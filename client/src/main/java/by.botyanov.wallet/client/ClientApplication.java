package by.botyanov.wallet.client;

import by.botyanov.wallet.server.model.WalletServiceGrpc;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Lists;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
public class ClientApplication implements ApplicationRunner {

    private static final String USERS_OPT = "users";
    private static final String CONCURRENT_THREADS_PER_USER_OPT = "concurrent_threads_per_user";
    private static final String ROUNDS_PER_THREAD = "rounds_per_thread";

    private ManagedChannel managedChannel;
    private WalletServiceGrpc.WalletServiceBlockingStub walletService;

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

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setBannerMode(Banner.Mode.OFF);
        app.run(args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        if (!args.containsOption(USERS_OPT) || !args.containsOption(CONCURRENT_THREADS_PER_USER_OPT)
                || !args.containsOption(ROUNDS_PER_THREAD)) {
            throw new Exception(USERS_OPT + ", " + CONCURRENT_THREADS_PER_USER_OPT + ", " + ROUNDS_PER_THREAD
                    + " options are required to run");
        }

        final Integer usersCount = Integer.valueOf(args.getOptionValues(USERS_OPT).get(0));
        final Integer concurrentThreads = Integer.valueOf(args.getOptionValues(CONCURRENT_THREADS_PER_USER_OPT).get(0));
        final Integer roundsPerThread = Integer.valueOf(args.getOptionValues(ROUNDS_PER_THREAD).get(0));

        final int threadCount = usersCount * concurrentThreads;

        final List<ClientEmulator> clients = Lists.newArrayListWithCapacity(threadCount);
        for (int i = 0; i < usersCount; i++) {
            for (int j = 0; j < concurrentThreads; j++) {
                final ClientEmulator clientEmulator = new ClientEmulator(i, walletService, roundsPerThread);
                clients.add(clientEmulator);
            }
        }
        final ExecutorService executorService = Executors.newFixedThreadPool(threadCount);

        final Stopwatch stopwatch = Stopwatch.createStarted();

        final List<Future<Void>> futures = executorService.invokeAll(clients);

        log.info("Done. Time elapsed: {} ms", stopwatch.elapsed(TimeUnit.MILLISECONDS));

        System.exit(0);
    }

}

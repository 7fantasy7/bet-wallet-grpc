package by.botyanov.wallet.server;

import by.botyanov.wallet.server.model.Balance;
import by.botyanov.wallet.server.model.BalanceResponse;
import by.botyanov.wallet.server.model.Currency;
import by.botyanov.wallet.server.model.Deposit;
import by.botyanov.wallet.server.model.WalletServiceGrpc;
import by.botyanov.wallet.server.model.Withdraw;
import io.grpc.ManagedChannel;
import io.grpc.StatusRuntimeException;
import io.grpc.inprocess.InProcessChannelBuilder;
import lombok.NonNull;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.lognet.springboot.grpc.GRpcServerRunner;
import org.lognet.springboot.grpc.autoconfigure.GRpcServerProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.util.TestPropertyValues;
import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.PostgreSQLContainer;

import javax.annotation.Nonnull;
import java.util.Map;

import static by.botyanov.wallet.common.ErrorCodes.INSUFFICIENT_FUNDS;
import static by.botyanov.wallet.server.model.Currency.EUR;
import static by.botyanov.wallet.server.model.Currency.USD;
import static org.junit.Assert.assertEquals;

@RunWith(SpringRunner.class)
@EnableAutoConfiguration
@ContextConfiguration(classes = ServerApplication.class,
        initializers = {WalletServerTest.Initializer.class, ConfigFileApplicationContextInitializer.class})
@Sql("/schema.sql")
public class WalletServerTest {

    @ClassRule
    public static PostgreSQLContainer<?> postgreSQLContainer = new PostgreSQLContainer<>("postgres:11.1");

    @Autowired
    protected GRpcServerProperties gRpcServerProperties;

    @Autowired
    @Qualifier("grpcInprocessServerRunner")
    protected GRpcServerRunner serverRunner;

    private ManagedChannel inProcessChannel;

    @Before
    public void setUp() throws Exception {
        serverRunner.run();

        inProcessChannel = InProcessChannelBuilder.forName(gRpcServerProperties.getInProcessServerName())
                .directExecutor().build();
    }

    @After
    public void tearDown() {
        inProcessChannel.shutdownNow();
    }

    @Test
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void testWalletService() {
        final WalletServiceGrpc.WalletServiceBlockingStub walletService =
                WalletServiceGrpc.newBlockingStub(inProcessChannel);

        final Runnable withdraw200USD = () -> walletService.withdraw(Withdraw.newBuilder()
                .setCurrency(USD).setAmount(200).setUserId(1).build());

        assertExceptionMessage(withdraw200USD, INSUFFICIENT_FUNDS);

        walletService.deposit(Deposit.newBuilder().setCurrency(USD).setAmount(100).setUserId(1).build());

        final BalanceResponse balance = walletService.balance(Balance.newBuilder().setUserId(1).build());
        assertBalances(balance, Map.of(USD, 100d));

        assertExceptionMessage(withdraw200USD, INSUFFICIENT_FUNDS);

        walletService.deposit(Deposit.newBuilder().setCurrency(EUR).setAmount(100).setUserId(1).build());

        final BalanceResponse balance1 = walletService.balance(Balance.newBuilder().setUserId(1).build());
        assertBalances(balance1, Map.of(USD, 100d, EUR, 100d));

        assertExceptionMessage(withdraw200USD, INSUFFICIENT_FUNDS);

        walletService.deposit(Deposit.newBuilder().setCurrency(USD).setAmount(100).setUserId(1).build());

        final BalanceResponse balance2 = walletService.balance(Balance.newBuilder().setUserId(1).build());
        assertBalances(balance2, Map.of(USD, 200d, EUR, 100d));

        walletService.withdraw(Withdraw.newBuilder().setCurrency(USD).setAmount(200).setUserId(1).build());

        final BalanceResponse balance3 = walletService.balance(Balance.newBuilder().setUserId(1).build());
        assertBalances(balance3, Map.of(USD, 0d, EUR, 100d));

        assertExceptionMessage(withdraw200USD, INSUFFICIENT_FUNDS);
    }

    private static void assertExceptionMessage(Runnable runnable, @NonNull String expectedMessage) {
        try {
            runnable.run();
            throw new AssertionError("Exception hasn't been raised");
        } catch (StatusRuntimeException e) {
            final String errorMessage = e.getStatus().getDescription();
            if (!expectedMessage.equals(errorMessage)) {
                throw new AssertionError(String.format("Error message differs, expected: %s, got: %s", expectedMessage, errorMessage));
            }
        }
    }

    private static void assertBalances(BalanceResponse balance, Map<Currency, Double> expected) {
        assertEquals(expected.size(), balance.getBalanceCount());

        for (Currency currency : expected.keySet()) {
            assertEquals(expected.get(currency), balance.getBalanceMap().get(currency.toString()));
        }
    }

    static class Initializer
            implements ApplicationContextInitializer<ConfigurableApplicationContext> {
        public void initialize(@Nonnull ConfigurableApplicationContext configurableApplicationContext) {
            TestPropertyValues.of(
                    "spring.datasource.url=" + postgreSQLContainer.getJdbcUrl(),
                    "spring.datasource.username=" + postgreSQLContainer.getUsername(),
                    "spring.datasource.password=" + postgreSQLContainer.getPassword()
            ).applyTo(configurableApplicationContext.getEnvironment());
        }
    }

}
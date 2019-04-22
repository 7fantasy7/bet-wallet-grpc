package by.botyanov.wallet.server.service.impl;

import by.botyanov.wallet.server.domain.Wallet;
import by.botyanov.wallet.server.exception.WalletException;
import by.botyanov.wallet.server.model.BalanceResponse;
import by.botyanov.wallet.server.model.Currency;
import by.botyanov.wallet.server.repository.WalletRepository;
import by.botyanov.wallet.server.service.WalletService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

import static by.botyanov.wallet.common.ErrorCodes.INSUFFICIENT_FUNDS;
import static by.botyanov.wallet.common.ErrorCodes.NOT_POSITIVE_AMOUNT;
import static by.botyanov.wallet.common.ErrorCodes.UNKNOWN_CURRENCY;

@Service
@Transactional
@RequiredArgsConstructor
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;

    @Override
    public void deposit(long userId, Currency currency, double amount) {
        if (currency == Currency.UNRECOGNIZED) {
            throw new WalletException(UNKNOWN_CURRENCY);
        }
        if (amount <= 0) {
            throw new WalletException(NOT_POSITIVE_AMOUNT);
        }

        final Wallet wallet = walletRepository.findByUserIdAndCurrency(userId, currency)
                .orElseGet(() -> new Wallet().setUserId(userId).setCurrency(currency));

        wallet.setAmount(wallet.getAmount() + amount);
        walletRepository.save(wallet);
    }

    @Override
    public void withdraw(long userId, Currency currency, double amount) {
        if (currency == Currency.UNRECOGNIZED) {
            throw new WalletException(UNKNOWN_CURRENCY);
        }
        if (amount <= 0) {
            throw new WalletException(NOT_POSITIVE_AMOUNT);
        }

        final Optional<Wallet> walletOpt = walletRepository.findByUserIdAndCurrency(userId, currency);
        walletOpt.ifPresentOrElse(wallet -> {
            if (wallet.getAmount() < amount) {
                throw new WalletException(INSUFFICIENT_FUNDS);
            }

            wallet.setAmount(wallet.getAmount() - amount);
            walletRepository.save(wallet);
        }, () -> {
            throw new WalletException(INSUFFICIENT_FUNDS);
        });
    }

    @Override
    public BalanceResponse balance(long userId) { // todo not gRPC response class
        final List<Wallet> userWallets = walletRepository.findByUserId(userId);
        if (userWallets.isEmpty()) {
            throw new WalletException(INSUFFICIENT_FUNDS);
        }

        final BalanceResponse.Builder builder = BalanceResponse.newBuilder();
        for (Wallet userWallet : userWallets) {
            builder.putBalance(userWallet.getCurrency().toString(), userWallet.getAmount());
        }

        return builder.build();
    }

}

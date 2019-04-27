package by.botyanov.wallet.server.service.impl;

import by.botyanov.wallet.server.domain.Wallet;
import by.botyanov.wallet.server.exception.WalletException;
import by.botyanov.wallet.server.model.Currency;
import by.botyanov.wallet.server.repository.WalletRepository;
import by.botyanov.wallet.server.service.WalletService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import static by.botyanov.wallet.common.ErrorCodes.INSUFFICIENT_FUNDS;
import static by.botyanov.wallet.common.ErrorCodes.NOT_POSITIVE_AMOUNT;
import static by.botyanov.wallet.common.ErrorCodes.UNKNOWN_CURRENCY;

@Slf4j
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

        log.debug("User {} makes deposit of {}{}", userId, currency, amount);

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

            log.debug("User {} withdraws {}{}", userId, currency, amount);

            wallet.setAmount(wallet.getAmount() - amount);
            walletRepository.save(wallet);
        }, () -> {
            throw new WalletException(INSUFFICIENT_FUNDS);
        });
    }

    @Transactional(readOnly = true)
    @Override
    public Map<Currency, Double> balance(long userId) {
        final List<Wallet> userWallets = walletRepository.findByUserId(userId);
        if (userWallets.isEmpty()) {
            throw new WalletException(INSUFFICIENT_FUNDS);
        }

        log.debug("User {} checks balance", userId);

        return userWallets.stream().collect(Collectors.toMap(Wallet::getCurrency, Wallet::getAmount));
    }

}

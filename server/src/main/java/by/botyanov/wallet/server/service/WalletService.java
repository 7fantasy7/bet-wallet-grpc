package by.botyanov.wallet.server.service;

import by.botyanov.wallet.server.model.BalanceResponse;

public interface WalletService {

    void deposit(long userId, by.botyanov.wallet.server.model.Currency currency, double amount);

    void withdraw(long userId, by.botyanov.wallet.server.model.Currency currency, double amount);

    BalanceResponse balance(long userId);

}

package by.botyanov.wallet.server.service;

import java.util.Map;

public interface WalletService {

    void deposit(long userId, by.botyanov.wallet.server.model.Currency currency, double amount);

    void withdraw(long userId, by.botyanov.wallet.server.model.Currency currency, double amount);

    Map<by.botyanov.wallet.server.model.Currency, Double> balance(long userId);

}

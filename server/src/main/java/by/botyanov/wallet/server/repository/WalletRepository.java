package by.botyanov.wallet.server.repository;

import by.botyanov.wallet.server.domain.Wallet;
import by.botyanov.wallet.server.model.Currency;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.stereotype.Repository;

import javax.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {

    // todo test if it works
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<Wallet> findByUserIdAndCurrency(Long userId, Currency currency);

    List<Wallet> findByUserId(Long userId);

}

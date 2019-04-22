package by.botyanov.wallet.server.grpc;

import by.botyanov.wallet.server.model.Balance;
import by.botyanov.wallet.server.model.BalanceResponse;
import by.botyanov.wallet.server.model.Deposit;
import by.botyanov.wallet.server.model.Empty;
import by.botyanov.wallet.server.model.WalletServiceGrpc;
import by.botyanov.wallet.server.model.Withdraw;
import by.botyanov.wallet.server.service.WalletService;
import io.grpc.stub.StreamObserver;
import lombok.RequiredArgsConstructor;
import org.lognet.springboot.grpc.GRpcService;

// todo LOGGING
// todo create normal service. and grpc on top of it? DONE
@GRpcService
@RequiredArgsConstructor
public class GrpcWalletService extends WalletServiceGrpc.WalletServiceImplBase {

    private final WalletService walletService;

    @Override
    public void deposit(Deposit request, StreamObserver<Empty> responseObserver) {
        try {
            walletService.deposit(request.getUserId(), request.getCurrency(), request.getAmount());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void withdraw(Withdraw request, StreamObserver<Empty> responseObserver) {
        try {
            walletService.withdraw(request.getUserId(), request.getCurrency(), request.getAmount());
            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

    @Override
    public void balance(Balance request, StreamObserver<BalanceResponse> responseObserver) {
        try {
            final BalanceResponse balance = walletService.balance(request.getUserId());
            responseObserver.onNext(balance);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        }
    }

}

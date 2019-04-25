package by.botyanov.wallet.server.grpc.interceptor;

import io.grpc.ForwardingServerCallListener;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@GRpcGlobalInterceptor
public class PerformanceMeasurementInterceptor implements ServerInterceptor {

    private final AtomicInteger counter = new AtomicInteger(0);

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        final ServerCall.Listener<ReqT> reqTListener = next.startCall(call, headers);

        return new ForwardingServerCallListener.SimpleForwardingServerCallListener<>(reqTListener) {
            @Override
            public void onComplete() {
                counter.incrementAndGet();
                super.onComplete();
            }
        };
    }

    @Scheduled(fixedDelay = 1000)
    public void logRequestCount() {
        final int reqs = counter.get();
        if (reqs > 0) {
            log.info("Reqs per second: {}", reqs);
            counter.set(0);
        }
    }

}
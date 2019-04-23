package by.botyanov.wallet.server.grpc.interceptor;

import io.grpc.ForwardingServerCall;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;
import io.grpc.Status;
import org.lognet.springboot.grpc.GRpcGlobalInterceptor;

@GRpcGlobalInterceptor
public class ExceptionHandlingInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers,
                                                                 ServerCallHandler<ReqT, RespT> next) {
        final ServerCall<ReqT, RespT> wrappedCall = new ForwardingServerCall.SimpleForwardingServerCall<>(call) {
            @Override
            public void close(Status status, Metadata trailers) {
                //todo log
                if (status.getCode() == Status.Code.UNKNOWN && status.getCause() != null) {
                    Throwable e = status.getCause();
                    status = Status.INTERNAL.withDescription(e.getMessage()).withCause(e);
                }
                super.close(status, trailers);
            }
        };

        return next.startCall(wrappedCall, headers);
    }

}
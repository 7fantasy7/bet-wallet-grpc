package by.botyanov.wallet.client.event.impl;

/**
 * @param <T> gRPC request type
 */
public interface Event<T> {

    T toRequest(Long userId);

}

package by.botyanov.wallet.common;

public class ErrorCodes {

    public static final String INSUFFICIENT_FUNDS = "insufficient_funds";
    public static final String UNKNOWN_CURRENCY = "unknown_currency";
    public static final String NOT_POSITIVE_AMOUNT = "not_positive_amount";

    private ErrorCodes() {
        throw new UnsupportedOperationException();
    }

}
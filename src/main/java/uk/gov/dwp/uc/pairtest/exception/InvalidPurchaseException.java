package uk.gov.dwp.uc.pairtest.exception;

public class InvalidPurchaseException extends RuntimeException {

    private final ErrorCode errorCode;

    public InvalidPurchaseException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }


    public enum ErrorCode {
        INVALID_ACCOUNT_ID,
        INVALID_NUMBER_OF_TICKETS,
        NO_ADULT_TICKETS,
        NOT_ENOUGH_ADULT_TICKETS
    }
}

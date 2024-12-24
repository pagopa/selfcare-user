package it.pagopa.selfcare.user.exception;

public class NumberOfAdminsExceededException extends RuntimeException {

    public NumberOfAdminsExceededException(String message) {
        super(message);
    }

}

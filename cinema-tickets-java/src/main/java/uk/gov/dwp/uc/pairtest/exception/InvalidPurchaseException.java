package uk.gov.dwp.uc.pairtest.exception;

public class InvalidPurchaseException extends RuntimeException {

	private static final long serialVersionUID = -1610931681935929029L;

	public InvalidPurchaseException(String message) {
        super(message);
    }
}

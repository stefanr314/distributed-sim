package rs.ac.bg.etf.domain.exceptions;

public class DomainException extends RuntimeException {
	public DomainException(String message) {
		super(message);
	}

	public DomainException(String message, Throwable e) {
		super(message, e);
	}
}
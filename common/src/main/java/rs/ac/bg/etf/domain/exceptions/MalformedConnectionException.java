package rs.ac.bg.etf.domain.exceptions;

public class MalformedConnectionException extends DomainException {
	public MalformedConnectionException(String message) {
		super(message);
	}

	public MalformedConnectionException(String message, Throwable cause) {
		super(message, cause);
	}
}
package rs.ac.bg.etf.domain.exceptions;

public class InvalidNumberOfPortsException extends DomainException {
	public InvalidNumberOfPortsException() {
		super("Number of ports must be positive number.");
	}
}
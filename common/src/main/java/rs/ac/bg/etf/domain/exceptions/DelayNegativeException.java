package rs.ac.bg.etf.domain.exceptions;

public class DelayNegativeException extends DomainException {
	public DelayNegativeException() {
		super("Component's delay field must be non-negative value.");
	}
}
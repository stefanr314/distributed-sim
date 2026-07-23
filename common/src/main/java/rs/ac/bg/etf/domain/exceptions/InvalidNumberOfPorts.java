package rs.ac.bg.etf.domain.exceptions;

public class InvalidNumberOfPorts extends DomainException {
	public InvalidNumberOfPorts() {
		super("Number of ports must be positive number.");
	}
}
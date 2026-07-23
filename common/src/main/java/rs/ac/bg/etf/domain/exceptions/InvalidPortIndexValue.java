package rs.ac.bg.etf.domain.exceptions;

public class InvalidPortIndexValue extends DomainException {
	public InvalidPortIndexValue() {
		super("Provided port index value must be non-negative value.");
	}
}
package rs.ac.bg.etf.domain.exceptions;

public class InvalidPortIndexValueException extends DomainException {
	public InvalidPortIndexValueException(int portValue) {
		super("Provided port index value must be non-negative value. Value provided: " + portValue);
	}
}
package rs.ac.bg.etf.domain.exceptions;

public class ComputeNullInputValueException extends DomainException {
	public ComputeNullInputValueException() {
		super("Computing on null values is not permitted.");
	}
}
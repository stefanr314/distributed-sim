package rs.ac.bg.etf.domain.exceptions;

public class InvalidSizeOfInputValues extends DomainException {
	public InvalidSizeOfInputValues(int expected, int actual) {
		super("Invalid size of input values of component's input ports. Expected size: %d, but received: %d".formatted(expected, actual));
	}
}
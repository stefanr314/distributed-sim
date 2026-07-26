package rs.ac.bg.etf.domain.exceptions;

public class PortIndexOutOfBoundExcepton extends DomainException {
	private final static String message = "Port index provided is out of bounds. This component does not consists of that many ports. Zero based " +
			"numeration is used.";

	public PortIndexOutOfBoundExcepton() {
		super(message);
	}
}
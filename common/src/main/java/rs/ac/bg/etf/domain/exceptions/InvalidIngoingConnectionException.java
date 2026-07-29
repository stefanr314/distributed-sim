package rs.ac.bg.etf.domain.exceptions;

import rs.ac.bg.etf.domain.component.ComponentId;

public class InvalidIngoingConnectionException extends DomainException {
	public InvalidIngoingConnectionException(int toPort, ComponentId id2) {
		super("Invalid ingoing connection creation attempt to component: " + id2.toString() + "to port: " + toPort +
				". Port index out of bounds.");
	}
}
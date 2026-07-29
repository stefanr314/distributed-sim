package rs.ac.bg.etf.domain.exceptions;

import rs.ac.bg.etf.domain.component.ComponentId;

public class InvalidOutgoingConnectionException extends DomainException {
	public InvalidOutgoingConnectionException(int fromPort, ComponentId id1) {
		super("Invalid attempt of outgoing connection on component:" + id1.toString() + "from port:" + fromPort + ". " +
				"Index value is invalid.");
	}
}
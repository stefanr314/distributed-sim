package rs.ac.bg.etf.domain.exceptions;

import rs.ac.bg.etf.domain.component.ComponentId;

public class DuplicateConnectionOnSinglePortException extends DomainException {
	public DuplicateConnectionOnSinglePortException(ComponentId id, int toPort) {
		super("Duplicate connection attempt on single port, for component:" + id.toString() + " on port:" + toPort);
	}
}
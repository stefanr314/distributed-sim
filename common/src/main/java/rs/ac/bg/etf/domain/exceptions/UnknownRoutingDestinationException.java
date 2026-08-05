package rs.ac.bg.etf.domain.exceptions;

import rs.ac.bg.etf.domain.component.ComponentId;

public class UnknownRoutingDestinationException extends DomainException {
	public UnknownRoutingDestinationException(ComponentId componentId) {
		super("Unknown routing destination for component: " + componentId.toString());
	}
}
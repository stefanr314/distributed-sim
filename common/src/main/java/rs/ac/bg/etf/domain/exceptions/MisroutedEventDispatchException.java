package rs.ac.bg.etf.domain.exceptions;

import rs.ac.bg.etf.domain.component.ComponentId;

public class MisroutedEventDispatchException extends DomainException {
	public MisroutedEventDispatchException(ComponentId componentId) {
		super("Event for component %s, was not present in simulator's netlist".formatted(componentId.toString()));
	}
}
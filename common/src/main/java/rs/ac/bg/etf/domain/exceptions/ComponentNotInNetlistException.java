package rs.ac.bg.etf.domain.exceptions;

import rs.ac.bg.etf.domain.component.ComponentId;

public class ComponentNotInNetlistException extends DomainException {
	public ComponentNotInNetlistException(ComponentId id) {
		super("Component with id: " + id.toString() + " is not in netlist. Therefore, it can not be reached.");
	}
}
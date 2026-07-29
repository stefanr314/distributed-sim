package rs.ac.bg.etf.domain.exceptions;

import rs.ac.bg.etf.domain.component.ComponentId;

public class DuplicateComponentInNetlistException extends DomainException {
	public DuplicateComponentInNetlistException(ComponentId id) {
		super("Component with id: " + id.toString() + " already contained in netlist. Duplicate component ids not " +
				"allowed.");
	}
}
package rs.ac.bg.etf.domain.exceptions;

import rs.ac.bg.etf.domain.component.ComponentId;

public class MisroutedEventException extends DomainException {
	public MisroutedEventException(ComponentId excepted, ComponentId received) {
		super(("Misrouting of events just occurred. Excepted component with id %s, " +
				"but received component id: %s").formatted(excepted.componentId(), received.componentId()));
	}
}
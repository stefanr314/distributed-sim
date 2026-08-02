package rs.ac.bg.etf.domain.exceptions;

import rs.ac.bg.etf.domain.InputPortKey;

public class MisroutedEventBufferReceiveException extends DomainException {
	public MisroutedEventBufferReceiveException(InputPortKey inputPortKey) {
		super("Event for component: %s, on port: %d, is not coming from external channel of simulator."
				.formatted(inputPortKey.componentId().toString(), inputPortKey.onPort()));
	}
}
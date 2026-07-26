package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.connection.Connection;

import java.util.List;

public class OrGateLogicalComponent extends LogicalComponent {
	public OrGateLogicalComponent(ComponentId componentId, ComponentPort<Boolean> inputPort,
	                              ComponentPort<Boolean> outputPort, long delay, List<Connection> outgoing) {
		super(componentId, inputPort, outputPort, delay, outgoing);
	}

	@Override
	protected Boolean computeValues(List<Boolean> inputs) {
		ensureAllInputValuesNotNull(inputs);
		
		ensureExpectedNumberOfInputValues(2, inputs);

		return inputs.get(0) || inputs.get(1);
	}
}
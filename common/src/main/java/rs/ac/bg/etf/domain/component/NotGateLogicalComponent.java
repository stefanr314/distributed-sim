package rs.ac.bg.etf.domain.component;

import java.util.List;

/**
 * Class {@code NotGateLogicalComponent} acts as the concrete not logical components. It's solely purpose is to invert
 * the received value on input port.
 */
public class NotGateLogicalComponent extends LogicalComponent {
	/**
	 * Default values of input and output ports and known and should not be passed as constructions arguments.
	 *
	 * @param componentId - id of component, string representation of component.
	 * @param delay       - delay that component brings to the distributed network.
	 */
	public NotGateLogicalComponent(ComponentId componentId, long delay) {
		super(componentId, ComponentPort.singlePort(), ComponentPort.singlePort(), delay);
	}

	/**
	 * Method that does the actual computation of inverted value on input port. Method is declared protected since
	 * it's only purpose is to be used as the part of parent's {@code LogicalComponent} API method {@code execute}.
	 * Method uses the parent's helper method {@code ensureExpectedNumberOfInputValues} to ensure proper number of
	 * input ports values (one in this case).
	 *
	 * @param inputs {@code List<Boolean>} the list of values on input port. There is only one element in the list
	 * @return {@code Boolean} inverted value of input port.
	 */
	@Override
	protected Boolean computeValues(List<Boolean> inputs) {
		ensureAllInputValuesNotNull(inputs);

		// this check becomes redundant with the construction fix implemented
//		ensureExpectedNumberOfInputValues(1, inputs);

		return !inputs.get(0);
	}
}
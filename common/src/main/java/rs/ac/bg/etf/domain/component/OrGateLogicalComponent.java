package rs.ac.bg.etf.domain.component;

import java.util.List;

/**
 * Class {@code OrGateLogicalComponent} is the concrete two-input OR logical gate. Its output is
 * {@code true} whenever at least one of its two inputs is {@code true}.
 *
 * @author stefanr
 * @since 1.0
 */
public class OrGateLogicalComponent extends LogicalComponent {
	/**
	 * Default values of input and output ports and known and should not be passed as constructions arguments.
	 *
	 * @param componentId - id of component, string representation of component.
	 * @param delay       - delay that component brings to the distributed network.
	 */
	public OrGateLogicalComponent(ComponentId componentId,
	                              long delay) {
		super(componentId, ComponentPort.fromNumber(2), ComponentPort.singlePort(), delay);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns the logical OR of the two input values.
	 *
	 * @param inputs the two current input values, in port-index order
	 * @return {@code true} if either input is {@code true}, {@code false} otherwise
	 * @throws rs.ac.bg.etf.domain.exceptions.ComputeNullInputValueException if either input is {@code null}
	 * @throws rs.ac.bg.etf.domain.exceptions.InvalidSizeOfInputValues       if {@code inputs} does not have exactly two elements
	 */
	@Override
	protected Boolean computeValues(List<Boolean> inputs) {
		ensureAllInputValuesNotNull(inputs);

		// this check becomes redundant with the construction fix implemented
//		ensureExpectedNumberOfInputValues(2, inputs);

		return inputs.get(0) || inputs.get(1);
	}
}
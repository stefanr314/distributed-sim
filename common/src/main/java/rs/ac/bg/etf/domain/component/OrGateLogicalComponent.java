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
	public OrGateLogicalComponent(ComponentId componentId, ComponentPort<Boolean> inputPort,
	                              ComponentPort<Boolean> outputPort, long delay) {
		super(componentId, inputPort, outputPort, delay);
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

		ensureExpectedNumberOfInputValues(2, inputs);

		return inputs.get(0) || inputs.get(1);
	}
}
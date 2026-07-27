package rs.ac.bg.etf.domain.component;

import java.util.List;

/**
 * Class {@code AndGateLogicalComponent} is the concrete two-input AND logical gate. Its output is
 * {@code true} only when both of its inputs are {@code true}.
 *
 * @author stefanr
 * @since 1.0
 */
public class AndGateLogicalComponent extends LogicalComponent {
	public AndGateLogicalComponent(ComponentId componentId, ComponentPort<Boolean> inputPort,
	                               ComponentPort<Boolean> outputPort, long delay) {
		super(componentId, inputPort, outputPort, delay);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Returns the logical AND of the two input values.
	 *
	 * @param inputs the two current input values, in port-index order
	 * @return {@code true} only if both inputs are {@code true}, {@code false} otherwise
	 * @throws rs.ac.bg.etf.domain.exceptions.ComputeNullInputValueException if either input is {@code null}
	 * @throws rs.ac.bg.etf.domain.exceptions.InvalidSizeOfInputValues       if {@code inputs} does not have exactly two elements
	 */
	@Override
	protected Boolean computeValues(List<Boolean> inputs) {
		ensureAllInputValuesNotNull(inputs);
		//ensure two inputs have arisen
		ensureExpectedNumberOfInputValues(2, inputs);
		//return the and gate logic
		return inputs.get(0) && inputs.get(1);

	}
}
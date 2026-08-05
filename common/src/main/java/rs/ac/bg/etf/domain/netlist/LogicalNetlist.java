package rs.ac.bg.etf.domain.netlist;

import rs.ac.bg.etf.domain.component.*;
import rs.ac.bg.etf.domain.exceptions.MalformedComponentDataException;

import java.util.Map;

/**
 * Class {@code LogicalNetlist} is the concrete {@link Netlist} for Boolean combinational circuits —
 * netlists composed entirely of logic gates ({@link LogicalGateTypes}). Component tokens are parsed via
 * {@link LogicalComponentComposer} and resolved to a concrete gate implementation through a static
 * type-to-builder registry. Concrete Creator class.
 *
 * @author stefanr
 * @since 1.0
 */
public class LogicalNetlist extends Netlist<Boolean> {
	private static final Map<LogicalGateTypes, Builder> BUILDERS = Map.of(
			LogicalGateTypes.AND, AndGateLogicalComponent::new,
			LogicalGateTypes.OR, OrGateLogicalComponent::new,
			LogicalGateTypes.NOT, NotGateLogicalComponent::new);

	protected LogicalNetlist() {
	}

	/**
	 * @return a new, empty {@code LogicalNetlist} ready to accept components and connections
	 */
	public static LogicalNetlist create() {
		return new LogicalNetlist();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Expects tokens in the format consumed by {@link LogicalComponentComposer}: gate type, component
	 * id, number of input ports, delay. The output port is always a single port, implicit for every
	 * gate type currently supported.
	 *
	 * @throws MalformedComponentDataException if {@code data} cannot be parsed, or names an unsupported gate type
	 */
	@Override
	protected Component<Boolean> createComponent(String[] data) {
		LogicalComponentComposer composer = LogicalComponentComposer.parse(data);
		LogicalGateTypes logicalGateType = composer.type();
		Builder builder = BUILDERS.get(logicalGateType);

		if (builder == null)
			throw new MalformedComponentDataException(
					"Type provided is not supported. Provided type:" + logicalGateType);

		ComponentId id = composer.componentId();
		long delay = composer.delay();

		return builder.build(
				id, delay
		);
	}

	@FunctionalInterface
	private interface Builder {
		Component<Boolean> build(ComponentId id, long delay);
	}
}
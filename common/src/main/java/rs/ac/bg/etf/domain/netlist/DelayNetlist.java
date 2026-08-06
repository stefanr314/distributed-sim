package rs.ac.bg.etf.domain.netlist;

import org.jetbrains.annotations.Contract;
import rs.ac.bg.etf.domain.component.*;
import rs.ac.bg.etf.domain.exceptions.MalformedComponentDataException;

import java.io.Serializable;

/**
 * Class {@code DelayNetlist} is a {@link Netlist} who's every component is a {@link DelayOnlyComponent} —
 * a pass-through component that forwards its input unchanged after a configured propagation delay,
 * with no logical transformation. Usable for any value type {@code V}.
 *
 * @param <V> the value type propagated by every component in this netlist
 * @author stefanr
 * @since 1.0
 */
public class DelayNetlist<V extends Serializable> extends Netlist<V> {
	@Contract(" -> new")
	public static <V extends Serializable> DelayNetlist<V> create() {
		return new DelayNetlist<>();
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Expects tokens in the format consumed by {@link DelayComponentComposer}: component id, delay.
	 * Both input and output ports are single ports, since {@link DelayOnlyComponent} forwards from
	 * port 0 to port 0 only.
	 *
	 * @throws MalformedComponentDataException if {@code data} cannot be parsed
	 */
	@Override
	protected Component<V> createComponent(String[] data) {
		DelayComponentComposer composer = DelayComponentComposer.parse(data);

		ComponentId id = composer.componentId();
		ComponentPort<V> inputPort = ComponentPort.singlePort();
		ComponentPort<V> outputPort = ComponentPort.singlePort();
		long delay = composer.delay();

		return new DelayOnlyComponent<>(id, inputPort, outputPort, delay);
	}
}
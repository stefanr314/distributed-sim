package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.DelayNegativeException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Value here depicts current value produced by the Component in discrete moment t. This value is meant to change
 * with time.
 *
 * @param <V>
 *
 */
public abstract class Component<V> {
	private final ComponentId componentId;
	private final ComponentPort<V> inputPort;
	private final ComponentPort<V> outputPort;
	private final long delay;
	private final List<Connection> outgoing;


	protected Component(ComponentId componentId, ComponentPort<V> inputPort, ComponentPort<V> outputPort, long delay,
	                    List<Connection> outgoing) {
		if (delay < 0) throw new DelayNegativeException();

		this.componentId = componentId;
		this.inputPort = Objects.requireNonNull(inputPort);
		this.outputPort = Objects.requireNonNull(outputPort);
		this.delay = delay;
		this.outgoing = List.copyOf(outgoing);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;

		if (o instanceof Component<?> that) {
			return this.componentId.equals(that.componentId);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(componentId);
	}

	public ComponentId componentId() {
		return componentId;
	}

	public ComponentPort<V> inputPort() {
		return inputPort;
	}

	public ComponentPort<V> outputPort() {
		return outputPort;
	}

	/**
	 * Method that returns value of the component in some discrete moment. Currently, assumption is that there is only
	 * one output port which also serves as the value holder of component, so the return value is the actual value on
	 * the single output port. The value might be null (which is true initially), so optional is returned. To be
	 * revisited if needed.
	 *
	 * @return {@code Optional<V>} value
	 */
	public Optional<V> value() {
		return outputPort.valueAtPort(0);
	}

	public List<V> inputValues() {
		if (!inputPort.allPortValuesSet()) return List.of();
		List<V> inputValues = new ArrayList<>();

		for (int i = 0; i < inputPort.numberOfPorts(); i++) {
			inputValues.add(inputPort.valueAtPort(i).orElse(null));
		}
		return inputValues;
	}

	public long delay() {
		return delay;
	}

	public List<Connection> outgoingConnections() {
		return List.copyOf(outgoing);
	}

	@Override
	public String toString() {
		return "Component{" +
				"componentId=" + componentId +
				", inputPort=" + inputPort +
				", outputPort=" + outputPort +
				", delay=" + delay +
				", value=" + value().toString() +
				'}';
	}


	public abstract List<Event<V>> execute(Event<V> msg);
}
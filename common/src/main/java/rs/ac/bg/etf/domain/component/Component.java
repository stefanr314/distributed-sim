package rs.ac.bg.etf.domain.component;

import java.util.Objects;

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
	private final V value;


	protected Component(ComponentId componentId, ComponentPort<V> inputPort, ComponentPort<V> outputPort, long delay,
	                    V value) {
		if (delay < 0) throw new AssertionError("Delay value has to be greater then zero");

		this.componentId = componentId;
		this.inputPort = Objects.requireNonNull(inputPort);
		this.outputPort = Objects.requireNonNull(outputPort);

		this.delay = delay;
		this.value = Objects.requireNonNull(value);
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

	public V getState() {
		return value;
	}

	@Override
	public String toString() {
		return "Component{" +
				"componentId=" + componentId +
				", inputPort=" + inputPort +
				", outputPort=" + outputPort +
				", delay=" + delay +
				", value=" + value +
				'}';
	}

	public long delay() {
		return delay;
	}

	public V value() {
		return value;
	}


	//	public abstract List<Event<V>> execute(Event<V> msg);
	public abstract Component<V> withValue(V newValue);
}
package rs.ac.bg.etf.domain.component;

import java.util.Objects;

public abstract class Component<V> {
	private final ComponentId componentId;
	private final ComponentPort inputPort;
	private final ComponentPort outputPort;
	private final long delay;
	private final V value;


	protected Component(ComponentId componentId, ComponentPort inputPort, ComponentPort outputPort, long delay,
	                    V value) {
		this.componentId = componentId;
		this.inputPort = Objects.requireNonNull(inputPort);
		this.outputPort = Objects.requireNonNull(outputPort);

		this.delay = delay;
		this.value = value;
	}

	// TODO: implement me
	// once uve written the equals method ask yourself questions: is it symmetric, is it transitive, is it consistent???
	@Override
	public boolean equals(Object o) {
		if (o == this) return true;
		if (o == null) return false; // instanceof does take care of this whatsoever 

		if (o instanceof Component<?> that) {
			return this.componentId.equals(that.componentId);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return Objects.hash(componentId, inputPort, outputPort, delay, value);
	}

	public ComponentId componentId() {
		return componentId;
	}

	public ComponentPort inputPort() {
		return inputPort;
	}

	public ComponentPort outputPort() {
		return outputPort;
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
	// TODO: implement rollback logic???
}
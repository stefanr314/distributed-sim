package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.DelayNegativeException;

import java.util.*;

/**
 * Class {@code Component<V>} serves as root class depicting key component API.
 *
 * @param <V> type parameter of component's value
 *
 */
public abstract class Component<V> {
	private final ComponentId componentId;
	private final ComponentPort<V> inputPort;
	private final ComponentPort<V> outputPort;
	private final long delay;
	private List<Connection> outgoing;


	protected Component(ComponentId componentId, ComponentPort<V> inputPort, ComponentPort<V> outputPort, long delay) {
		if (delay < 0) throw new DelayNegativeException();

		this.componentId = componentId;
		this.inputPort = Objects.requireNonNull(inputPort);
		this.outputPort = Objects.requireNonNull(outputPort);
		this.delay = delay;
		this.outgoing = Collections.emptyList();
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

	public long delay() {
		return delay;
	}

	public List<Connection> outgoingConnections() {
		return List.copyOf(outgoing);
	}


	/**
	 * A method that acts as mutator/setter method of outgoing component's connections, since this structure will be
	 * only provided after instantization of concrete component classes. Since the reference is younger than the
	 * component it will be additionally provided with respect of field referencing an empty list upon initialization.
	 *
	 * @param outgoing {@code List<Connection>} list of component's outgoing connections.
	 */
	public void attachOutgoingConnections(List<Connection> outgoing) {
		this.outgoing = List.copyOf(outgoing);
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

	protected List<V> inputValues() {
		if (!inputPort.allPortValuesSet()) return List.of();
		List<V> inputValues = new ArrayList<>();

		for (int i = 0; i < inputPort.numberOfPorts(); i++) {
			inputValues.add(inputPort.valueAtPort(i).orElse(null));
		}
		return inputValues;
	}


	@Override
	public String toString() {
		return "Component{" + "componentId=" + componentId + ", inputPort=" + inputPort + ", outputPort=" + outputPort + ", delay=" + delay + ", outgoing=" + outgoing + '}';
	}

	/**
	 * <p>
	 * Key method of distributed component's value evaluation. Adequate list of events is forwarded only upon
	 * achieving the internal component invariant state; otherwise no interaction with the rest of network is attained.
	 * The concrete computation is delegated to subclasses.
	 * </p>
	 *
	 * @param msg {@code Event<V>} a message received on component's input port.
	 * @return {@code List<Event<V>>} list of events as response to received message. If component's state invariant
	 * is not achieved, empty list is returned and no events are forwarded along the pipeline i.e. Netlist.
	 */
	public abstract List<Event<V>> execute(Event<V> msg);
}
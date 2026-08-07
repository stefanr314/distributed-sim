package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.DelayNegativeException;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Class {@code Component<V>} serves as root class depicting key component API. Each component has a
 * fixed identity ({@link ComponentId}), a fixed input/output port configuration, and a fixed
 * propagation delay — all set at construction and never mutated afterward. Port <em>values</em>,
 * however, are mutable: {@link ComponentPort} tracks the current value on each port as events are
 * processed, since a single component instance is repeatedly re-evaluated across many discrete time
 * moments during a simulation.
 * <p>
 * Outgoing connections are a special case: a component is constructed with none, and they are attached
 * one at a time afterward via {@link #attachOutgoingConnection(Connection)}. This reflects the netlist
 * file format itself — all components are declared first, then all connections referencing them — so a
 * component's wiring is necessarily incomplete at construction time and only becomes whole once its
 * owning {@code Netlist} has finished processing every connection that originates from it.
 * <p>
 * Equality and hashing are defined solely by {@link ComponentId} — two {@code Component} instances
 * sharing the same id are treated as the same logical component regardless of their current port state,
 * consistent with a component's identity surviving serialization across the network while its state
 * does not need to.
 * <p>
 * Subclasses supply the actual computation via {@link #execute(Event)}; this class deliberately knows
 * nothing about network transport, simulation strategy (conservative vs. optimistic), or which of
 * its connections are local versus external to the partition it happens to run in — those are the
 * responsibility of {@code Netlist} and {@code Simulator} respectively. Components without a delay are not supported
 * in distributed systems and can produce deadlocks.
 *
 * @param <V> type parameter of component's value
 * @author stefanr
 * @since 1.0
 */
public abstract class Component<V extends Serializable> implements Serializable {
	@Serial
	private static final long serialVersionUID = 4L;

	private final ComponentId componentId;
	private final ComponentPort<V> inputPort;
	private final ComponentPort<V> outputPort;
	private final long delay;
	private final List<Connection> outgoing = new ArrayList<>();

	/**
	 *
	 * @param componentId - id/name of component
	 * @param inputPort   - component port resembling the input port numbers and values present at them
	 * @param outputPort  - component port resembling the output port numbers and values present at them
	 * @param delay       - delay equal to zero cannot be accepted since it will result in false promise of clock's
	 *                    time advancement at the begging of simulation leading to the deadlock.
	 */
	protected Component(ComponentId componentId, ComponentPort<V> inputPort, ComponentPort<V> outputPort, long delay) {
		if (delay <= 0) throw new DelayNegativeException();

		this.componentId = Objects.requireNonNull(componentId);
		this.inputPort = Objects.requireNonNull(inputPort);
		this.outputPort = Objects.requireNonNull(outputPort);
		this.delay = delay;
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
	 * Registers {@code connection} as one of this component's outgoing connections, consulted by
	 * {@link #execute(Event)} when addressing the events it produces. Intended to be called exclusively
	 * by the owning {@code Netlist}, immediately after a connection originating from this component has
	 * been validated and added to it — calling this directly, bypassing the owning {@code Netlist}, will
	 * cause this component's outgoing connections to diverge from what the netlist itself reports.
	 *
	 * @param connection a connection whose source is this component's id
	 */
	public void attachOutgoingConnection(Connection connection) {
		outgoing.add(connection);
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
		return "Component{" + "componentId=" + componentId + ", " +
				"inputPort=" + inputPort + ", " +
				"outputPort=" + outputPort + ", " +
				"delay=" + delay + ", " +
				"outgoing=" + outgoing + '}';
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
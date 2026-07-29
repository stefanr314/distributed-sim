package rs.ac.bg.etf.domain.netlist;

import rs.ac.bg.etf.domain.component.Component;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.connection.ConnectionAssembler;
import rs.ac.bg.etf.domain.exceptions.*;

import java.util.*;

/**
 * Class {@code Netlist<V>} is the aggregate root of a simulated circuit: it owns every {@link Component}
 * and every {@link Connection} between them, and is the single source of truth for both. Components and
 * connections are added incrementally, in two separate passes, mirroring the netlist file format
 * (all components first, then all connections referencing them by id).
 * <p>
 * Concrete component creation is delegated to {@link #createComponent(String[])}, implemented by
 * subclasses per component family (e.g. {@link LogicalNetlist} for Boolean logic gates), with respect to Factory
 * Method Pattern.
 *
 * @param <V> the value type produced and consumed by every component in this netlist
 * @author stefanr
 * @since 1.0
 */
public abstract class Netlist<V> {
	private final Map<ComponentId, Component<V>> components;
	private final List<Connection> connections;

	protected Netlist() {
		this.components = new HashMap<>();
		this.connections = new ArrayList<>();
	}

	/**
	 * @return an unmodifiable snapshot of every component currently in this netlist, keyed by id
	 */
	public Map<ComponentId, Component<V>> components() {
		return Map.copyOf(components);
	}

	/**
	 * @return an unmodifiable snapshot of every connection currently in this netlist
	 */
	public List<Connection> connections() {
		return List.copyOf(connections);
	}

	/**
	 * Parses {@code data} into a {@link Connection} and adds it to this netlist, provided both endpoint
	 * components already exist and the referenced port indices are valid on each. Self-loops (a
	 * component connected back to itself) are permitted, as are multiple connections sharing the same
	 * source port (fan-out) — only multiple connections driving the same target port are rejected.
	 * <p>
	 * Also this method serves as a connector for component's outgoing connections. On this way a components gets
	 * filled with connections required for proper functioning of message passing events and data distribution.
	 * Currently, this is the only way to add connections on component. Every other way of manipulating the
	 * component's outgoing connections breaks the invariant of the class.
	 * </p>
	 *
	 * @param data tokenized connection data, in the format expected by {@link ConnectionAssembler}
	 * @throws MisroutedConnectionInNetlist             if either endpoint reference is null
	 * @throws ComponentNotInNetlistException           if either endpoint component was never added via {@link #addComponent(String[])}
	 * @throws InvalidOutgoingConnectionException       if the source port index is out of bounds for the source component
	 * @throws InvalidIngoingConnectionException        if the target port index is out of bounds for the target component
	 * @throws DuplicateConnectionOnSinglePortException if the target port already has a connection driving it
	 */
	public void addConnection(String[][] data) {
		Connection connection = ConnectionAssembler.assemble(data);

		isValidConnectionBetweenComponents(
				connection.source(),
				connection.target(),
				connection.fromPort(),
				connection.toPort()
		);
		checkDualConnectionOnSinglePort(
				connection.target(),
				connection.toPort()
		);

		connections.add(connection);
		components.get(connection.source()).attachOutgoingConnection(connection);
	}

	/**
	 * Parses {@code data} via {@link #createComponent(String[])} and adds the resulting component to
	 * this netlist.
	 *
	 * @param data tokenized component data, in the format expected by this netlist's concrete
	 *             {@link #createComponent(String[])} implementation
	 * @throws DuplicateComponentInNetlistException if a component with the same id was already added
	 */
	public void addComponent(String[] data) {
		Component<V> component = createComponent(data);

		componentAlreadyContained(component.componentId());

		components.put(component.componentId(), component);
	}

	/**
	 * Collects the current output value of every component in this netlist, as required for reporting
	 * simulation results back up the chain (workstation to server to client). A component's value is
	 * absent ({@link Optional#empty()}) if it has not yet produced any output.
	 *
	 * @return one entry per component currently in this netlist, in no particular order
	 */
	public List<Pair<ComponentId, Optional<V>>> getState() {
		return components.entrySet().stream()
				.map((e) ->
						new Pair<>(
								e.getKey(),
								e.getValue().value()
						)
				)
				.toList();
	}

	/**
	 * Looks up every connection whose source is {@code id} — the set of downstream components this
	 * component must notify when it produces a new output value.
	 *
	 * @param id id of the component whose outgoing connections are requested
	 * @return connections originating from {@code id}, or an empty list if it has none
	 * @throws ComponentNotInNetlistException if {@code id} does not identify a component in this netlist
	 */
	public List<Connection> outgoingConnectionsFrom(ComponentId id) {
		if (!components.containsKey(id)) throw new ComponentNotInNetlistException(id);

		List<Connection> outgoing = new ArrayList<>();
		for (Connection connection : connections) {
			if (connection.source().equals(id)) outgoing.add(connection);
		}

		//fixme: attach them

		return outgoing;
	}


	private void isValidConnectionBetweenComponents(ComponentId id1, ComponentId id2, int fromPort, int toPort) {
		// if component id1 or id2 not found in not map of components, a misrouted connection occurred
		if (id1 == null || id2 == null) throw new MisroutedConnectionInNetlist(
				(id1 == null ? "Source" : "Target") + " component reference in connection must not be null");

		//get both components
		Component<V> fromComponent = components.get(id1);
		if (fromComponent == null) throw new ComponentNotInNetlistException(id1);
		Component<V> toComponent = components.get(id2);
		if (toComponent == null) throw new ComponentNotInNetlistException(id2);

		// check for valid from and to port properties on individual components
		if (!fromComponent.outputPort().isValidIndexValue(fromPort))
			throw new InvalidOutgoingConnectionException(fromPort, id1);
		if (!toComponent.inputPort().isValidIndexValue(toPort))
			throw new InvalidIngoingConnectionException(toPort, id2);

	}

	// I just suck at coding. Easter egg xD.
	private void checkDualConnectionOnSinglePort(ComponentId id, int toPort) {
		// check for null value
		Objects.requireNonNull(id);
		if (!components.containsKey(id)) throw new ComponentNotInNetlistException(id);

		// iterate through connection list and look a match
		for (Connection connection : connections) {
			if (connection.target().equals(id) && connection.toPort() == toPort)
				throw new DuplicateConnectionOnSinglePortException(id, toPort);
		}
	}

	private void componentAlreadyContained(ComponentId id) {
		if (components.containsKey(id)) throw new DuplicateComponentInNetlistException(id);

	}

	/**
	 * Interprets tokenized component data and constructs the appropriate concrete {@link Component}
	 * subclass. Implemented per component family — see {@link LogicalNetlist} for an example resolving
	 * a gate-type token to one of several concrete gate classes via a factory lookup.
	 *
	 * @param data tokenized component data
	 * @return a fully constructed, not-yet-connected component
	 */
	protected abstract Component<V> createComponent(String[] data);
}
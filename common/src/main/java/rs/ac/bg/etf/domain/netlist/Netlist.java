package rs.ac.bg.etf.domain.netlist;

import rs.ac.bg.etf.domain.component.Component;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.connection.Connection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//todo consider ids on netlist; where that can come handy
public abstract class Netlist<V> {
	private final Map<ComponentId, Component<V>> components;
	private List<Connection> connections;

	//todo: consider visibility and other ways of construction
	protected Netlist() {
		this.components = new HashMap<>();
		this.connections = new ArrayList<>();
	}

	// okay lets thing do I need list of components or of component ids; the catch in my opinion is that I don't have
	// a way to reach the components with their ids, because there is no persistence mechanism - UR JUST STUPID THERE
	// IS A MAP ALWAYS MY FRIEND - I just suck at coding.

	// also this class is supposed to have limitation in terms of valid states of both components and connections
	private void isValidConnectionBetweenComponents(ComponentId id1, ComponentId id2) {

	}

	private void checkDualConnectionOnSinglePort(ComponentId id) {

	}

	//todo consider feedback loops as valid states?
	private void componentAlreadyContained(ComponentId id) {

	}

	// also parsing is required of input data but the catch is that that behaviour can be delegated to the
	// appropriate entry point i.e. client part; parsing should not be accessible from every other module, unless
	// there is a higher demand for that behaviour (I don't see the one currently). This class still receives
	// tokenized data from outside parser
	//todo consider tokenized data entry
	public void addConnection(String[][] data) {
		// from received tokenized data perform the connection creation; no factory needed since it's pretty
		// straightforward process

		// check for validity

		// perform insertion into the list
	}

	//todo: use enums at data[0] does this make sense?
	public void addComponent(String[] data) {
		// use the factory to create a proper component
		Component<V> component = createComponent(data);

		// check for validity
		componentAlreadyContained(component.componentId());

		// insert it in the map if valid
		components.put(component.componentId(), component);
	}

	//todo implement check the types...
	public List<Pair<ComponentId, V>> getState() {
		return List.of();
	}

	// reach connections of some components method
	public List<Connection> outgoingConnectionsFrom(ComponentId id) {
		return List.of();
	}

	protected abstract Component<V> createComponent(String[] data);

	// everything has to implement serializable

	// defensive copies on components and connections
}
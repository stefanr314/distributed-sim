package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.MisroutedEventException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DelayOnlyComponent<V> extends Component<V> {
	public DelayOnlyComponent(ComponentId componentId, ComponentPort<V> inputPort, ComponentPort<V> outputPort,
	                          long delay, List<Connection> outgoing) {
		super(componentId, inputPort, outputPort, delay, outgoing);
	}

	@Override
	public List<Event<V>> execute(Event<V> msg) {
		//unpack the event msg and set values
		if (!msg.destinationComponent().equals(componentId())) throw new MisroutedEventException(componentId(),
				msg.destinationComponent());
		inputPort().setValueAtPort(msg.value(), msg.atPort());

		//send data to other ports if ready, if not return empty list; just forward data
		if (!inputPort().allPortValuesSet()) return List.of();

		// set output value in this case value of the component
		Optional<V> optVal = inputPort().valueAtPort(0);
		V calculatedValue = optVal.orElse(null);
		outputPort().setValueAtPort(calculatedValue, 0);

		long timeMoment = msg.atDiscreteTimeMoment() + delay();
		List<Event<V>> listOfEvents = new ArrayList<>();

		for (Connection connection : outgoingConnections()) {
			Event<V> event = new Event<>(connection.target(), connection.toPort(), calculatedValue, timeMoment);

			listOfEvents.add(event);
		}

		return listOfEvents;
	}
}
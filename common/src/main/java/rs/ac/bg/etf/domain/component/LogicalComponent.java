package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.MisroutedEventException;

import java.util.ArrayList;
import java.util.List;

public abstract class LogicalComponent extends Component<Boolean> {
	protected LogicalComponent(ComponentId componentId, ComponentPort<Boolean> inputPort, ComponentPort<Boolean> outputPort, long delay, List<Connection> outgoing) {
		super(componentId, inputPort, outputPort, delay, outgoing);
	}

	@Override
	public List<Event<Boolean>> execute(Event<Boolean> msg) {
		if (!msg.destinationComponent().equals(componentId())) throw new MisroutedEventException(componentId(),
				msg.destinationComponent());
		inputPort().setValueAtPort(msg.value(), msg.atPort());

		//send data to other ports if ready, if not return empty list; just forward data
		if (!inputPort().allPortValuesSet()) return List.of();

		//set value on output port i.e. this component's value
		Boolean calculatedValue = computeValues(inputValues());
		outputPort().setValueAtPort(calculatedValue, 0);

		long timeMoment = msg.atDiscreteTimeMoment() + delay();

		List<Event<Boolean>> listOfEvents = new ArrayList<>();

		for (Connection connection : outgoingConnections()) {

			Event<Boolean> event = new Event<>(connection.target(), connection.toPort(), calculatedValue, timeMoment);

			listOfEvents.add(event);
		}

		return listOfEvents;
	}

	protected abstract Boolean computeValues(List<Boolean> inputs);
}
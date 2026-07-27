package rs.ac.bg.etf.domain.component;

import org.jetbrains.annotations.NotNull;
import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.MisroutedEventException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DelayOnlyComponent<V> extends Component<V> {
	public DelayOnlyComponent(ComponentId componentId, ComponentPort<V> inputPort, ComponentPort<V> outputPort,
	                          long delay) {
		super(componentId, inputPort, outputPort, delay);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation does not transform the received value in any way — its sole purpose is to
	 * introduce the configured {@code delay} into the simulation while forwarding the input value
	 * unchanged to every connected downstream component. Useful for modeling wires or transmission
	 * lines where no logical computation takes place, only propagation delay.
	 * <p>
	 * The input value is read from port {@code 0} only; this implementation assumes a single-input
	 * component and does not support fan-in from multiple sources.
	 * <p>
	 * If this component currently has no outgoing connections (e.g. it acts as a terminal probe within
	 * the netlist), the returned list is empty even though the internal state (and output port value)
	 * has still been updated — the value remains retrievable via {@link #value()}.
	 *
	 * @param msg the incoming event carrying the value to forward
	 * @return a list of events addressed to each connected downstream component, or an empty list if
	 * not all input port values have arrived yet, or if there are no outgoing connections
	 * @throws MisroutedEventException if {@code msg} is addressed to a different component than this one
	 */
	@Override
	public List<Event<V>> execute(@NotNull Event<V> msg) {
		if (!msg.destinationComponent().equals(componentId())) throw new MisroutedEventException(componentId(),
				msg.destinationComponent());
		inputPort().setValueAtPort(msg.value(), msg.atPort());

		if (!inputPort().allPortValuesSet()) return List.of();

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
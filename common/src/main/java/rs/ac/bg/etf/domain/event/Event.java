package rs.ac.bg.etf.domain.event;

import rs.ac.bg.etf.domain.component.ComponentId;

/**
 * Class {@code Event<V>} represents the event passed between components which holds the value
 * {@code V Value} available at component
 * with {@code ComponentId} id in discrete time moment {@code atDiscreteTimeMoment} on port {@code atPort}. The class
 * is immutable.
 *
 * @param <V> value intended for component at discrete time moment
 * @author stefanr
 * @since version 1.0
 */
public final class Event<V> {
	private final ComponentId destinationComponent;
	private final int atPort;
	private final V value;
	private final long atDiscreteTimeMoment;

	public Event(ComponentId destinationComponent, int atPort, V value, long atDiscreteTimeMoment) {
		this.destinationComponent = destinationComponent;
		this.atPort = atPort;
		this.value = value;
		this.atDiscreteTimeMoment = atDiscreteTimeMoment;
	}

	public ComponentId destinationComponent() {
		return destinationComponent;
	}

	public int atPort() {
		return atPort;
	}

	public V value() {
		return value;
	}

	public long atDiscreteTimeMoment() {
		return atDiscreteTimeMoment;
	}

	@Override
	public String toString() {
		return "Event{" +
				"destinationComponent=" + destinationComponent +
				", atPort=" + atPort +
				", value=" + value +
				", atDiscreteTimeMoment=" + atDiscreteTimeMoment +
				'}';
	}
}
package rs.ac.bg.etf.domain.event;

import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.exceptions.InvalidPortIndexValueException;

import java.util.Objects;

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
	private int hash;

	public Event(ComponentId destinationComponent, int atPort, V value, long atDiscreteTimeMoment) {
		if (atPort < 0) throw new InvalidPortIndexValueException(atPort);

		this.destinationComponent = Objects.requireNonNull(destinationComponent, "Destination component id of event " +
				"must be different to null");
		this.atPort = atPort;
		this.value = value;
		this.atDiscreteTimeMoment = atDiscreteTimeMoment;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;

		if (!(o instanceof Event<?> event)) return false;

		return atPort == event.atPort && atDiscreteTimeMoment == event.atDiscreteTimeMoment
				&& destinationComponent.equals(event.destinationComponent) && Objects.equals(value, event.value);
	}

	@Override
	public int hashCode() {
		int result = hash;

		if (result == 0) {
			result = destinationComponent.hashCode();
			result = 31 * result + Integer.hashCode(atPort);
			result = 31 * result + Objects.hashCode(value);
			result = 31 * result + Long.hashCode(atDiscreteTimeMoment);
			hash = result;
		}

		return result;
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
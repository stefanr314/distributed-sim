package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.exceptions.InvalidPortIndexValue;

import java.util.Optional;

/**
 * This class serves as immutable class holder of port value within the component. Explicitly it is not an immutable
 * class but contains private constructor with publicly available factory static methods. Implementation of
 * {@code equals()}
 * and {@code hashCode()} serves no purpose since port values has no need for "logical equality" and its effectively
 * immutable
 * class so x.equals(y) if and only if x == y.
 * Defensive copies can not be conducted on generics since compiler has no prior knowledge on type. The contract of
 * this class relies on generic type representing the immutable class.
 *
 * @param <V> placeholder for value
 * @author stefanr
 * @since 1.0
 */
public class ComponentPortValue<V> {
	private final int portIndex;
	private final V value;

	private ComponentPortValue(V value, int portIndex) {
		if (portIndex < 0) throw new InvalidPortIndexValue();

		this.value = value;
		this.portIndex = portIndex;
	}

	public static <V> ComponentPortValue<V> initValueAtPort(int atPort) {
		return new ComponentPortValue<>(null, atPort);
	}

	public static <V> ComponentPortValue<V> fromValueAtPort(V value, int atPort) {
		return new ComponentPortValue<>(value, atPort);
	}

	public Optional<V> value() {
		return Optional.ofNullable(value);
	}

	public int portIndex() {
		return portIndex;
	}

	public boolean valueSet() {
		return value != null;
	}

	@Override
	public String toString() {
		return "ComponentPortValue{" +
				"portIndex=" + portIndex +
				", value=" + value +
				'}';
	}
}
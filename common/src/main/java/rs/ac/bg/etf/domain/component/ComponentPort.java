package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.exceptions.InvalidNumberOfPortsException;
import rs.ac.bg.etf.domain.exceptions.PortIndexOutOfBoundExcepton;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Class servers as holder of component's port and it's effectively immutable. No implementations of neither the
 * {@code equals()} nor the {@code hashCode()} methods since there is no need for "logical equality" of component's
 * ports e.g. x.equlas(y) if and only if x == y.
 *
 * @param <V> placeholder of port's value
 * @author stefanr
 * @since 1.0
 */
public class ComponentPort<V> {
	private final int numberOfPorts;
	private final List<ComponentPortValue<V>> portValues;

	private ComponentPort(int numberOfPorts) {
		this.numberOfPorts = numberOfPorts;

		this.portValues = initPortValues(numberOfPorts);
	}

	public static <V> ComponentPort<V> fromNumber(int numberOfPorts) {
		if (numberOfPorts < 1) throw new InvalidNumberOfPortsException();

		return new ComponentPort<>(numberOfPorts);
	}

	public static <V> ComponentPort<V> singlePort() {
		return new ComponentPort<>(1);
	}

	public void setValueAtPort(V value, int atPort) {
		checkPortIndexInBound(atPort);

		portValues.set(atPort, ComponentPortValue.fromValueAtPort(value, atPort));
	}


	public int numberOfPorts() {
		return this.numberOfPorts;
	}

	public boolean valueSetAtPort(int atPort) {
		checkPortIndexInBound(atPort);

		return portValues.get(atPort).valueSet();
	}

	public Optional<V> valueAtPort(int atPort) {
		checkPortIndexInBound(atPort);

		return portValues.get(atPort).value();
	}

	public boolean allPortValuesSet() {
		return portValues.stream().allMatch(ComponentPortValue::valueSet);
	}

	@Override
	public String toString() {
		return "ComponentPort{" +
				"numberOfPorts=" + numberOfPorts +
				", portValues=" + portValues +
				'}';
	}

	private void checkPortIndexInBound(int atPort) {
		if (atPort < 0 || atPort >= numberOfPorts) throw new PortIndexOutOfBoundExcepton();
	}

	private List<ComponentPortValue<V>> initPortValues(int numberOfPorts) {
		List<ComponentPortValue<V>> list = new ArrayList<>();

		for (int i = 0; i < numberOfPorts; i++) {
			list.add(ComponentPortValue.initValueAtPort(i));
		}

		return list;
	}
}
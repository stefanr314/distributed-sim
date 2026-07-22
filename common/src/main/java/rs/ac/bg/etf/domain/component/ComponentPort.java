package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.exceptions.PortIndexOutOfBound;

import java.util.Arrays;

public final class ComponentPort {
	private final int numberOfPorts;
	private final int[] port;
	private final int hash;

	// FIXME: not thread safe
	private ComponentPort(int numberOfPorts) {
		this.numberOfPorts = numberOfPorts;
		this.port = new int[numberOfPorts];
		this.hash = Integer.hashCode(numberOfPorts);

		for (int i = 0; i < numberOfPorts; i++) {
			port[i] = i;
		}
	}

	public static ComponentPort fromNumber(int numberOfPorts) {
		// FIXME: throw appropriate domain exception
		if (numberOfPorts < 1) throw new AssertionError();

		return new ComponentPort(numberOfPorts);
	}

	public static ComponentPort singlePort() {
		return new ComponentPort(1);
	}

	public int numberOfPorts() {
		return this.numberOfPorts;
	}

	public int portAt(int index) {
		// guard clause
		if (index > this.numberOfPorts || index < 0) throw new PortIndexOutOfBound();

		return this.port[index];
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;

		if (!(o instanceof ComponentPort that)) return false;

		return numberOfPorts == that.numberOfPorts;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return "ComponentPort{" +
				"numberOfPorts=" + numberOfPorts +
				", port=" + Arrays.toString(port) +
				'}';
	}
}
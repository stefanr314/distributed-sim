package rs.ac.bg.etf.domain;

import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.connection.Connection;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class {@code InputPortKey} identifies a single input port of a single component — the (component,
 * port) pair a {@link Connection} targets. Used as the key for {@link ConservativeSimulator}'s external
 * channel clock tracking: since the netlist enforces at most one connection driving any given input
 * port, this pair is equivalent to identifying the external source feeding it.
 *
 * @author stefanr
 * @since 1.0
 */
public final class InputPortKey implements Serializable {
	@Serial
	private static final long serialVersionUID = 9L;
	
	private final ComponentId componentId;
	private final int onPort;


	public InputPortKey(ComponentId componentId, int onPort) {
		Objects.requireNonNull(componentId);

		this.componentId = componentId;
		this.onPort = onPort;
	}

	public ComponentId componentId() {
		return componentId;
	}

	public int onPort() {
		return onPort;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (!(o instanceof InputPortKey that)) return false;

		return onPort == that.onPort && this.componentId.equals(that.componentId);
	}

	@Override
	public int hashCode() {
		int result = Objects.hashCode(componentId);
		result = 31 * result + onPort;
		return result;
	}

	@Override
	public String toString() {
		return "InputPortKey{" +
				"componentId=" + componentId +
				", onPort=" + onPort +
				'}';
	}
}
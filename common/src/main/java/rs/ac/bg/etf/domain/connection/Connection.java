package rs.ac.bg.etf.domain.connection;

import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.exceptions.InvalidPortIndexValueException;

import java.io.Serial;
import java.io.Serializable;
import java.util.Objects;

/**
 * Class {@code Connection} provides a way to interconnect two components into a net. Components are described with
 * their representing ids through immutable class {@code ComponentId}, and connected on provided ports {@code
 * fromPort} and {@code toPort}. Implementations of {@code equals()} and {@code hashCode()} are done
 * in deed of providing easier duplicate connection check, that will be conducted by appropriate class/classes.
 *
 * @author stefanr
 * @since 1.0
 */
public final class Connection implements Serializable {
	@Serial
	private static final long serialVersionUID = 5L;

	private final ComponentId sourceComponent;
	private final ComponentId targetComponent;
	private final int fromPort;
	private final int toPort;
	private int hash;

	public Connection(ComponentId sourceComponent, ComponentId targetComponent, int fromPort, int toPort) {
		checkPortIndexValidity(fromPort, toPort);

		this.sourceComponent = Objects.requireNonNull(sourceComponent, "Source component must be provided.");
		this.targetComponent = Objects.requireNonNull(targetComponent, "Target component must be provided.");
		this.fromPort = fromPort;
		this.toPort = toPort;
	}

	private void checkPortIndexValidity(int fromPort, int toPort) {
		if (fromPort < 0 || toPort < 0) throw new InvalidPortIndexValueException(fromPort < 0 ? fromPort : toPort);
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true;

		if (!(o instanceof Connection that)) return false;

		return fromPort == that.fromPort && toPort == that.toPort && sourceComponent.equals(that.sourceComponent) && targetComponent.equals(that.targetComponent);
	}

	@Override
	public int hashCode() {
		int result = hash;

		if (result == 0) {
			result = sourceComponent.hashCode();
			result = 31 * result + targetComponent.hashCode();
			result = 31 * result + Integer.hashCode(fromPort);
			result = 31 * result + Integer.hashCode(toPort);
			hash = result;
		}

		return result;
	}

	public ComponentId source() {
		return sourceComponent;
	}

	public ComponentId target() {
		return targetComponent;
	}

	public int fromPort() {
		return fromPort;
	}

	public int toPort() {
		return toPort;
	}

	@Override
	public String toString() {
		return "Connection{" +
				"sourceComponent=" + sourceComponent +
				", targetComponent=" + targetComponent +
				", fromPort=" + fromPort +
				", toPort=" + toPort +
				'}';
	}
}
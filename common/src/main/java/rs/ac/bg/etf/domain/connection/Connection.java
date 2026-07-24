package rs.ac.bg.etf.domain.connection;

import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.exceptions.InvalidPortIndexValueException;

/**
 * Class {@code Connection} provides a way to interconnect two components into a net. Components are described with
 * their representing ids through immutable class {@code ComponentId}, and connected on provided ports {@code
 * fromPort} and {@code toPort}.
 *
 * @author stefanr
 */
public final class Connection {
	private final ComponentId sourceComponent;
	private final ComponentId targetComponent;
	private final int fromPort;
	private final int toPort;

	public Connection(ComponentId sourceComponent, ComponentId targetComponent, int fromPort, int toPort) {
		checkPortIndexValidity(fromPort, toPort);

		this.sourceComponent = sourceComponent;
		this.targetComponent = targetComponent;
		this.fromPort = fromPort;
		this.toPort = toPort;
	}

	private void checkPortIndexValidity(int fromPort, int toPort) {
		if (fromPort < 0 || toPort < 0) throw new InvalidPortIndexValueException(fromPort < 0 ? fromPort : toPort);
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
package rs.ac.bg.etf.domain.connection;

import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.exceptions.MalformedConnectionException;

/**
 * Class that serves as an assembler of {@code Connection} from input data provided as {@code String[][]} data. Class
 * acts as a non-instantiable immutable class.
 */
public final class ConnectionAssembler {
	private ConnectionAssembler() {
	}

	public static Connection assemble(String[][] data) {
		if (data == null) throw new MalformedConnectionException("Data to be parsed was found as a null reference.");

		try {
			String[] from = data[0];
			String[] to = data[1];

			ComponentId fromComponentId = new ComponentId(from[0]);
			int fromPort = Integer.parseInt(from[1]);

			ComponentId toComponentId = new ComponentId(to[0]);
			int toPort = Integer.parseInt(to[1]);

			return new Connection(fromComponentId, toComponentId, fromPort, toPort);
		} catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
			throw new MalformedConnectionException("Parsing connection data failed.", e);
		}

	}
}
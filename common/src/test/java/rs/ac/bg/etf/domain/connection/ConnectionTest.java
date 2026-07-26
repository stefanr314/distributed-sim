package rs.ac.bg.etf.domain.connection;

import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.exceptions.InvalidPortIndexValueException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


class ConnectionTest {

	/**
	 * This test provides a point of connection that can connect the same component on different ports. This
	 * behaviour is not perhaps wanted and will be the responsibility of other layer to cope with.
	 */
	@Test
	public void testConnectionEquality() {
		//Act
		ComponentId cid1 = new ComponentId("AA");
		ComponentId cid2 = new ComponentId("AA");

		int fromPort = 1;
		int toPort = 2;

		Connection con1 = new Connection(cid1, cid2, fromPort, toPort);
		Connection con2 = new Connection(cid1, cid2, fromPort, toPort);

		assertThat(con1).isEqualTo(con2);
	}

	@Test
	public void throwExceptionWhenPortHasNegativeValue() {
		ComponentId cid1 = new ComponentId("AA");
		ComponentId cid2 = new ComponentId("AA");

		int fromPort = 1;
		int toPort = -2;

		assertThatExceptionOfType(InvalidPortIndexValueException.class)
				.isThrownBy(() -> new Connection(cid1, cid2,
						fromPort, toPort))
				.withMessage("Provided port index value must be non-negative value. Value provided: -2");

	}

	@Test
	public void throwExceptionUponNullReferencesOnComponentIds() {
		ComponentId cid2 = new ComponentId("AA");

		int fromPort = 1;
		int toPort = 2;

		assertThatExceptionOfType(NullPointerException.class)
				.isThrownBy(() -> new Connection(null, cid2,
						fromPort, toPort))
				.withMessage("Source component must be provided.");

	}
}
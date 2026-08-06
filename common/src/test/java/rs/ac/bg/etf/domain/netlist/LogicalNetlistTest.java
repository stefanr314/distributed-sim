package rs.ac.bg.etf.domain.netlist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.exceptions.ComponentNotInNetlistException;
import rs.ac.bg.etf.domain.exceptions.DuplicateComponentInNetlistException;
import rs.ac.bg.etf.domain.exceptions.DuplicateConnectionOnSinglePortException;
import rs.ac.bg.etf.domain.exceptions.InvalidIngoingConnectionException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class LogicalNetlistTest {

	private LogicalNetlist netlist;

	@BeforeEach
	void setUp() {
		netlist = LogicalNetlist.create();
	}

	@Test
	void addComponentAddsToComponentsMap() {
		netlist.addComponent(new String[]{"AND", "G1", "2", "5"});

		assertThat(netlist.components()).containsKey(new ComponentId("G1"));
	}

	@Test
	void addComponentRejectsDuplicateId() {
		netlist.addComponent(new String[]{"AND", "G1", "2", "5"});

		assertThatExceptionOfType(DuplicateComponentInNetlistException.class)
				.isThrownBy(() -> netlist.addComponent(new String[]{"OR", "G1", "2", "3"}));
	}

	@Test
	void addConnectionRejectsUnknownSourceComponent() {
		netlist.addComponent(new String[]{"AND", "G2", "2", "5"});

		String[][] connectionData = {{"G1", "0"}, {"G2", "0"}};

		assertThatExceptionOfType(ComponentNotInNetlistException.class)
				.isThrownBy(() -> netlist.addConnection(connectionData));
	}

	@Test
	void addConnectionRejectsOutOfBoundsTargetPort() {
		netlist.addComponent(new String[]{"NOT", "G1", "1", "2"});
		netlist.addComponent(new String[]{"NOT", "G2", "1", "2"});

		String[][] connectionData = {{"G1", "0"}, {"G2", "5"}};

		assertThatExceptionOfType(InvalidIngoingConnectionException.class)
				.isThrownBy(() -> netlist.addConnection(connectionData));
	}

	@Test
	void addConnectionRejectsSecondDriverOnSameTargetPort() {
		netlist.addComponent(new String[]{"NOT", "G1", "1", "1"});
		netlist.addComponent(new String[]{"NOT", "G2", "1", "1"});
		netlist.addComponent(new String[]{"NOT", "G3", "1", "1"});
		netlist.addConnection(new String[][]{{"G1", "0"}, {"G3", "0"}});

		String[][] secondDriver = {{"G2", "0"}, {"G3", "0"}};

		assertThatExceptionOfType(DuplicateConnectionOnSinglePortException.class)
				.isThrownBy(() -> netlist.addConnection(secondDriver));
	}

	@Test
	void outgoingConnectionsFromReturnsOnlyConnectionsOriginatingAtGivenComponent() {
		netlist.addComponent(new String[]{"NOT", "G1", "1", "1"});
		netlist.addComponent(new String[]{"NOT", "G2", "1", "1"});
		netlist.addComponent(new String[]{"NOT", "G3", "1", "1"});
		netlist.addConnection(new String[][]{{"G1", "0"}, {"G2", "0"}});
		netlist.addConnection(new String[][]{{"G1", "0"}, {"G3", "0"}});

		List<rs.ac.bg.etf.domain.connection.Connection> outgoing =
				netlist.outgoingConnectionsFrom(new ComponentId("G1"));

		assertThat(outgoing).hasSize(2);
	}

	@Test
	void outgoingConnectionsFromThrowsForUnknownComponent() {
		assertThatExceptionOfType(ComponentNotInNetlistException.class)
				.isThrownBy(() -> netlist.outgoingConnectionsFrom(new ComponentId("GHOST")));
	}

	@Test
	void getStateReturnsEmptyOptionalForComponentsThatHaveNotExecutedYet() {
		netlist.addComponent(new String[]{"AND", "G1", "2", "5"});

		List<Pair<ComponentId, Boolean>> state = netlist.getState();

		assertThat(state).hasSize(1);
		assertThat(state.get(0).id()).isEqualTo(new ComponentId("G1"));
		assertThat(state.get(0).value()).isNull();
	}
}
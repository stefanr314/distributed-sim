package rs.ac.bg.etf.domain.component;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NotGateLogicalComponentTest {

	private NotGateLogicalComponent newGate(long delay, List<Connection> outgoing) {
		ComponentId id = new ComponentId("N1");
		ComponentPort<Boolean> input = ComponentPort.singlePort();
		ComponentPort<Boolean> output = ComponentPort.singlePort();
		return new NotGateLogicalComponent(id, input, output, delay, outgoing);
	}

	@ParameterizedTest
	@CsvSource({
			"true,  false",
			"false, true"
	})
	void executeInvertsSingleInput(boolean input, boolean expected) {
		NotGateLogicalComponent gate = newGate(2, List.of());

		gate.execute(new Event<>(gate.componentId(), 0, input, 7));

		assertThat(gate.value()).contains(expected);
	}

	@Test
	void executeProducesEventOnOutgoingConnection() {
		Connection toDownstream = new Connection(new ComponentId("N1"), new ComponentId("N2"), 0, 0);
		NotGateLogicalComponent gate = newGate(1, List.of(toDownstream));

		List<Event<Boolean>> result = gate.execute(new Event<>(gate.componentId(), 0, true, 7));

		assertThat(result).hasSize(1);
		assertThat(result.get(0).value()).isFalse();
		assertThat(result.get(0).atDiscreteTimeMoment()).isEqualTo(8); // 7 + delay(1)
	}
}
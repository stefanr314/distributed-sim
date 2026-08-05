package rs.ac.bg.etf.domain.component;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.ComputeNullInputValueException;
import rs.ac.bg.etf.domain.exceptions.MisroutedEventException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


class AndGateLogicalComponentTest {
	private @NotNull AndGateLogicalComponent newGate(long delay, List<Connection> outgoing) {
		ComponentId id = new ComponentId("G1");
		AndGateLogicalComponent andGateLogicalComponent = new AndGateLogicalComponent(id, delay);

		for (Connection connection : outgoing) {
			andGateLogicalComponent.attachOutgoingConnection(connection);
		}

		return andGateLogicalComponent;
	}

	@Test
	void executeReturnsEmptyListWhenOnlyOneInputArrived() {
		AndGateLogicalComponent gate = newGate(3, List.of());

		List<Event<Boolean>> result = gate.execute(new Event<>(gate.componentId(), 0, true, 10));

		assertThat(result).isEmpty();
		assertThat(gate.value()).isEmpty();
	}

	@Test
	void testComputeValuesWhenInputsPartiallyInitialized() {
		AndGateLogicalComponent gate = newGate(1, List.of());
		List<Boolean> partialInputs = Arrays.asList(Boolean.TRUE, null);

		assertThatExceptionOfType(ComputeNullInputValueException.class).isThrownBy(() -> gate.computeValues(partialInputs));
	}

	@ParameterizedTest
	@CsvSource({
			"true, true, true",
			"true, false, false",
			"false, true, false",
			"false, false, false",
	})
	void executeProducesCorrectAndGateValue(boolean firstValue, boolean secondValue, boolean expectedValue) {
		Connection toDownstream = new Connection(new ComponentId("G1"), new ComponentId("G2"), 0, 0);
		AndGateLogicalComponent gate = newGate(1, List.of(toDownstream));
		Event<Boolean> eventOnFirstPort = new Event<>(gate.componentId(), 0, firstValue, 10);
		Event<Boolean> eventOnSecondPort = new Event<>(gate.componentId(), 1, secondValue, 11);

		//act
		gate.execute(eventOnFirstPort);
		List<Event<Boolean>> events = gate.execute(eventOnSecondPort);
		Optional<Boolean> value = gate.value();

		//assert
		assertThat(value).contains(expectedValue);
		assertThat(events).hasSize(1);

		Event<Boolean> actualEvent = events.get(0);
		assertThat(actualEvent.value()).isEqualTo(expectedValue);
		assertThat(actualEvent.atDiscreteTimeMoment()).isEqualTo(12);
		assertThat(actualEvent.destinationComponent()).isEqualTo(new ComponentId("G2"));
	}

	@Test
	void executeThrowsWhenEventMisrouted() {
		AndGateLogicalComponent gate = newGate(1, List.of());
		Event<Boolean> misrouted = new Event<>(new ComponentId("WRONG"), 0, true, 5);

		assertThatExceptionOfType(MisroutedEventException.class)
				.isThrownBy(() -> gate.execute(misrouted));
	}
}
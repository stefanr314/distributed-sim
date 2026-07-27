package rs.ac.bg.etf.domain.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import rs.ac.bg.etf.domain.connection.Connection;
import rs.ac.bg.etf.domain.event.Event;
import rs.ac.bg.etf.domain.exceptions.ComputeNullInputValueException;
import rs.ac.bg.etf.domain.exceptions.InvalidSizeOfInputValues;
import rs.ac.bg.etf.domain.exceptions.MisroutedEventException;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;


class OrGateLogicalComponentTest {

	private OrGateLogicalComponent finalGate;
	private OrGateLogicalComponent gate;

	@BeforeEach
	void setUp() {
		ComponentId cid = new ComponentId("B1");
		ComponentPort<Boolean> in = ComponentPort.fromNumber(2);
		ComponentPort<Boolean> out = ComponentPort.singlePort();
		long delay1 = 5;
		long delay2 = 10;
		Connection con = new Connection(cid, new ComponentId("B2"), 0, 0);

		finalGate = new OrGateLogicalComponent(cid, in, out, delay1);
		gate = new OrGateLogicalComponent(cid, in, out, delay2);

		finalGate.attachOutgoingConnections(Collections.emptyList());
		gate.attachOutgoingConnections(List.of(con));
	}

	@Test
	void executeReturnsEmptyListWhenOnlyOneInputArrived() {
		List<Event<Boolean>> result = finalGate.execute(new Event<>(finalGate.componentId(), 0, true, 10));

		assertThat(result).isEmpty();
		assertThat(finalGate.value()).isEmpty();
	}

	@Test
	void testComputeValuesWhenInputsPartiallyInitialized() {
		List<Boolean> partialInputs = Arrays.asList(Boolean.TRUE, null);

		assertThatExceptionOfType(ComputeNullInputValueException.class).isThrownBy(() -> finalGate.computeValues(partialInputs));
	}

	@Test
	void computeValuesThrowsWhenWrongInputCount() {
		List<Boolean> tooFew = List.of(Boolean.TRUE);

		assertThatExceptionOfType(InvalidSizeOfInputValues.class)
				.isThrownBy(() -> finalGate.computeValues(tooFew));
	}

	@ParameterizedTest
	@CsvSource({
			"true, true, true",
			"true, false, true",
			"false, true, true",
			"false, false, false",
	})
	void executeProducesCorrectAndGateValue(boolean firstValue, boolean secondValue, boolean expectedValue) {
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
		assertThat(actualEvent.atDiscreteTimeMoment()).isEqualTo(21);
		assertThat(actualEvent.destinationComponent()).isEqualTo(new ComponentId("B2"));
	}

	@Test
	void executeThrowsWhenEventMisrouted() {
		Event<Boolean> misrouted = new Event<>(new ComponentId("WRONG"), 0, true, 5);

		assertThatExceptionOfType(MisroutedEventException.class)
				.isThrownBy(() -> finalGate.execute(misrouted));
	}
}
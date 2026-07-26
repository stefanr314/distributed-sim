package rs.ac.bg.etf.domain.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.exceptions.InvalidPortIndexValueException;

import static org.assertj.core.api.Assertions.*;

class EventTest {
	@Test
	void constructorRejectsNullDestination() {
		assertThatNullPointerException()
				.isThrownBy(() -> new Event<>(null, 0, Boolean.TRUE, 5))
				.withMessageContaining("Destination component id of event " +
						"must be different to null");
	}

	@ParameterizedTest
	@CsvSource({
			"A1, 0, true,  10, A1, 0, true,  10, true",   // identical
			"A1, 0, true,  10, A1, 0, false, 10, false",  // different value
			"A1, 0, true,  10, A1, 1, true,  10, false",  // different port
			"A1, 0, true,  10, A1, 0, true,  11, false",  // different time discrete moment
			"A1, 0, true,  10, A2, 0, true,  10, false"   // different destination
	})
	void equalsReflectsAllFields(String id1, int port1, boolean value1, long time1,
	                             String id2, int port2, boolean value2, long time2,
	                             boolean expectedEqual) {
		Event<Boolean> first = new Event<>(new ComponentId(id1), port1, value1, time1);
		Event<Boolean> second = new Event<>(new ComponentId(id2), port2, value2, time2);

		assertThat(first.equals(second)).isEqualTo(expectedEqual);
		if (expectedEqual) {
			assertThat(first.hashCode()).isEqualTo(second.hashCode());
		}
	}

	@Test
	public void testEventHoldsProperValue() {
		ComponentId cid1 = new ComponentId("A1");
		int toPort = 1;
		Boolean value = Boolean.TRUE;
		long time = 2;

		Event<Boolean> event1 = new Event<>(cid1, toPort, value, time);

		assertThat(event1.destinationComponent()).isNotNull();
		assertThat(event1.destinationComponent()).isEqualTo(cid1);
		assertThat(event1.value()).isTrue();
		assertThat(event1.atPort()).isEqualTo(1);
		assertThat(event1.atDiscreteTimeMoment()).isEqualTo(2);
	}

	@ParameterizedTest
	@CsvSource({"-1", "-2"})
	public void throwNewInvalidPortExceptionWhenPortNegative(int negativePort) {
		ComponentId cid1 = new ComponentId("A1");
		long time = 2;

		assertThatExceptionOfType(InvalidPortIndexValueException.class)
				.isThrownBy(() -> new Event<>(cid1, negativePort, Boolean.TRUE, time));
	}

}
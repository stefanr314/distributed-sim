package rs.ac.bg.etf.domain.event;

import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.exceptions.InvalidPortIndexValueException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class EventTest {

	@Test
	public void testClassImmutabilityEquality() {
		ComponentId cid1 = new ComponentId("A1");
		int toPort = 1;
		Boolean value = Boolean.TRUE;
		long time = 2;

		Event<Boolean> event1 = new Event<>(cid1, toPort, value, time);
		Event<Boolean> event2 = new Event<>(cid1, toPort, Boolean.TRUE, time);

		assertThat(event1).isEqualTo(event2);
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
		assertThat(event1.value()).isEqualTo(Boolean.TRUE);
		assertThat(event1.atPort()).isEqualTo(1);
		assertThat(event1.atDiscreteTimeMoment()).isEqualTo(2);
	}

	@Test
	public void throwNewInvalidPortExceptionWhenPortNegative() {
		ComponentId cid1 = new ComponentId("A1");
		int toPort = -1;
		Boolean value = Boolean.TRUE;
		long time = 2;

		assertThatExceptionOfType(InvalidPortIndexValueException.class)
				.isThrownBy(() -> new Event<Boolean>(cid1, toPort, value, time));
	}

}
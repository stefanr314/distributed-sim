package rs.ac.bg.etf.domain.component;

import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.domain.exceptions.InvalidPortIndexValueException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;


public class ComponentPortValueTest {

	@Test
	public void testInitValueAtPort() {
		int atPort1 = 1;
		int atPort2 = 3;

		//Act
		ComponentPortValue<Boolean> cpv1 = ComponentPortValue.initValueAtPort(atPort1);
		ComponentPortValue<Boolean> cpv2 = ComponentPortValue.initValueAtPort(atPort2);

		//Assert
		assertThat(cpv1).isNotEqualTo(cpv2);

		assertThat(cpv1.value()).isEqualTo(Optional.empty());
		assertThat(cpv2.value()).isEqualTo(Optional.empty());


		assertThat(cpv1.portIndex()).isEqualTo(1);
		assertThat(cpv2.portIndex()).isEqualTo(3);

	}

	@Test
	public void testCreateNewPortValueWithValue() {
		int atPort1 = 1;
		int atPort2 = 3;
		Boolean b1 = Boolean.FALSE;
		Boolean b2 = Boolean.TRUE;

		//Act
		ComponentPortValue<Boolean> cpv1 = ComponentPortValue.initValueAtPort(atPort1);

		ComponentPortValue<Boolean> cpv3 = ComponentPortValue.fromValueAtPort(b1, atPort1);
		ComponentPortValue<Boolean> cpv4 = ComponentPortValue.fromValueAtPort(b2, atPort2);

		//Assert
		assertThat(cpv1).isNotEqualTo(cpv3);
		assertThat(cpv1.portIndex()).isEqualTo(1);
		assertThat(cpv3.portIndex()).isEqualTo(1);

		assertThat(cpv1.value()).isEqualTo(Optional.empty());

		assertThat(cpv3.value()).isEqualTo(Optional.of(Boolean.FALSE));

		assertThat(cpv4.portIndex()).isEqualTo(3);
		assertThat(cpv4.value()).isEqualTo(Optional.of(Boolean.TRUE));
	}

	@Test
	public void testRaisesExceptionUponInvalidPortIndexValue() {
		int atPort = -1;

		Throwable thrown = catchThrowable(() -> ComponentPortValue.initValueAtPort(atPort));

		//Assert
		assertThat(thrown).isInstanceOf(InvalidPortIndexValueException.class)
				.hasMessageContaining("Provided port index value must be non-negative value.");
	}

	@Test
	public void testValueSetOnInitialValuePort() {
		//arrange
		int atPort1 = 1;
		ComponentPortValue<Boolean> cpv1 = ComponentPortValue.initValueAtPort(atPort1);

		//assert
		assertThat(cpv1.valueSet()).isFalse();
	}

	@Test
	public void testValueSetWhenPortValueContainsValue() {
		//arrange
		int atPort1 = 1;

		ComponentPortValue<Boolean> cpv1 = ComponentPortValue.fromValueAtPort(Boolean.FALSE, atPort1);

		//assert
		assertThat(cpv1.valueSet()).isTrue();
	}
}
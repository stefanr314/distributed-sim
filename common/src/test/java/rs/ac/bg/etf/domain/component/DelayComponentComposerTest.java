package rs.ac.bg.etf.domain.component;

import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.domain.exceptions.MalformedComponentDataException;

import static org.assertj.core.api.Assertions.*;

class DelayComponentComposerTest {
	@Test
	void parseValidDataSuccessfullyCreatesComposer() {
		// Arrange
		String[] data = new String[]{"DELAY_1", "10"};

		// Act
		DelayComponentComposer composer = DelayComponentComposer.parse(data);

		// Assert
		assertThat(composer)
				.returns(new ComponentId("DELAY_1"), from(DelayComponentComposer::componentId))
				.returns(10L, DelayComponentComposer::delay);
	}


	@Test
	void parseInvalidNumberFormatThrowsException() {
		String[] invalidNumberData = new String[]{"DELAY_1", "NaN"};

		assertThatExceptionOfType(MalformedComponentDataException.class)
				.isThrownBy(() -> DelayComponentComposer.parse(invalidNumberData))
				.withCauseInstanceOf(NumberFormatException.class);
	}
}
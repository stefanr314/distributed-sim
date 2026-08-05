package rs.ac.bg.etf.domain.component;

import org.junit.jupiter.api.Test;
import rs.ac.bg.etf.domain.exceptions.MalformedComponentDataException;

import static org.assertj.core.api.Assertions.*;

class LogicalComponentComposerTest {
	@Test
	void parseValidDataSuccessfullyCreatesComposer() {
		// Arrange
		String[] data = new String[]{"AND", "GATE_1", "10"};

		// Act
		LogicalComponentComposer composer = LogicalComponentComposer.parse(data);

		// Assert
		assertThat(composer)
				.returns(LogicalGateTypes.AND, from(LogicalComponentComposer::type))
				.returns(new ComponentId("GATE_1"), from(LogicalComponentComposer::componentId))
				.returns(10L, LogicalComponentComposer::delay);
	}


	@Test
	void parseInvalidEnumTypesThrowsException() {
		String[] invalidEnumData = new String[]{"UNKNOWN_GATE", "GATE_1", "10"};

		assertThatExceptionOfType(MalformedComponentDataException.class)
				.isThrownBy(() -> LogicalComponentComposer.parse(invalidEnumData))
				.withCauseInstanceOf(IllegalArgumentException.class);
	}

	@Test
	void parseInvalidNumberFormatThrowsException() {
		String[] invalidNumberData = new String[]{"AND", "GATE_1", "NaN"};

		assertThatExceptionOfType(MalformedComponentDataException.class)
				.isThrownBy(() -> LogicalComponentComposer.parse(invalidNumberData))
				.withCauseInstanceOf(NumberFormatException.class);
	}
}
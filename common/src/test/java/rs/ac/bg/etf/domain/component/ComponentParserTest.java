package rs.ac.bg.etf.domain.component;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullSource;
import rs.ac.bg.etf.domain.exceptions.MalformedComponentDataException;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.catchThrowable;
import static org.assertj.core.api.BDDAssertions.then;

class ComponentParserTest {
	private Function<String[], Integer> lameFunction;

	@BeforeEach
	void setUp() {
		lameFunction = (data) -> 1;
	}


	@ParameterizedTest
	@NullSource
	@DisplayName("Calling the parsing on null data throws MalformedComponentDataException")
	void throwUponInvalidDataPassed(String[] nullData) {
		// BDD style, AAA used across project but no harm done i guess.
		Throwable thrown = catchThrowable(() -> ComponentParser.parse(nullData, 1, lameFunction));

		then(thrown).isInstanceOf(MalformedComponentDataException.class)
				.hasMessageContaining("Expected at least 1 entries, but got: 0");

	}

	@Test
	void throwUponLessDataLengthThanExpected() {
		String[] data = {"Meaningless data"};
		// BDD style, AAA used across project but no harm done i guess.
		Throwable thrown = catchThrowable(() -> ComponentParser.parse(data, 3, lameFunction));

		then(thrown).isInstanceOf(MalformedComponentDataException.class)
				.hasMessage("Expected at least 3 entries, but got: 1");
	}
}
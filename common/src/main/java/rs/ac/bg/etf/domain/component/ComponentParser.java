package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.exceptions.MalformedComponentDataException;

import java.util.function.Function;

/**
 * Helper class used for parsing the input {@code String[] data} throughout only single static method
 * {@link #parse(String[], int, Function)}. Class behaves as non-instantiable immutable class.
 */
public final class ComponentParser {
	private ComponentParser() {
	}

	/**
	 * Parses {@code data} into a {@code V} via {@code createComposer}, after validating that {@code data}
	 * is non-null and has at least {@code expectedLength} entries. Any {@link IllegalArgumentException} or
	 * {@link IndexOutOfBoundsException} thrown by {@code createComposer} (e.g. {@link NumberFormatException}
	 * from a malformed numeric token, or {@link IllegalArgumentException} from an unknown enum constant) is
	 * caught and rethrown as {@link MalformedComponentDataException}, keeping every parsing failure in this
	 * project uniform regardless of which field caused it.
	 *
	 * @param data           tokenized input data
	 * @param expectedLength minimum number of tokens required
	 * @param createComposer constructs the target type from {@code data}, assuming it is well-formed
	 * @param <V>            the composer type produced
	 * @return the parsed composer
	 * @throws MalformedComponentDataException if {@code data} is null, too short, or {@code createComposer} fails
	 */
	public static <V> V parse(String[] data, int expectedLength, Function<String[], V> createComposer) {
		if (data == null || data.length < expectedLength) {
			throw new MalformedComponentDataException(
					"Expected at least " + expectedLength + " entries, but got: " + (data == null ? 0 : data.length)
			);
		}
		try {
			return createComposer.apply(data);
		} catch (IllegalArgumentException | IndexOutOfBoundsException e) {
			throw new MalformedComponentDataException(data, e);
		}
	}
}
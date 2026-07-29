package rs.ac.bg.etf.domain.exceptions;

public class MalformedComponentDataException extends DomainException {
	public MalformedComponentDataException(String[] data, Throwable cause) {
		super(formatEntryData(data), cause);

	}

	public MalformedComponentDataException(String message) {
		super(message);
	}

	private static String formatEntryData(String[] data) {
		if (data == null) return "null";

		StringBuilder sb = new StringBuilder("Component could not be constructed properly. Occurred on data: ");
		for (String datum : data) {
			sb.append(datum);
		}

		return sb.toString();
	}
}
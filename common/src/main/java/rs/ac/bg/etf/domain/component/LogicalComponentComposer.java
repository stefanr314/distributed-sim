package rs.ac.bg.etf.domain.component;

/**
 * Class that acts as the composer of logical component parts.
 */
public final class LogicalComponentComposer {
	private static final int EXPECTED_NUMBER_OF_ENTRIES = 4;

	private final LogicalGateTypes type;
	private final ComponentId componentId;
	private final int inputPorts;
	private final long delay;


	private LogicalComponentComposer(LogicalGateTypes type, ComponentId componentId, int inputPorts, long delay) {
		this.type = type;
		this.componentId = componentId;
		this.inputPorts = inputPorts;
		this.delay = delay;
	}

	public static LogicalComponentComposer parse(String[] data) {
		return ComponentParser.parse(
				data,
				EXPECTED_NUMBER_OF_ENTRIES,
				d -> new LogicalComponentComposer(
						Enum.valueOf(LogicalGateTypes.class, d[0]),
						new ComponentId(d[1]),
						Integer.parseInt(d[2]),
						Long.parseLong(d[3])
				));
	}

	public LogicalGateTypes type() {
		return type;
	}

	public ComponentId componentId() {
		return componentId;
	}

	public int inputPorts() {
		return inputPorts;
	}

	public long delay() {
		return delay;
	}

	@Override
	public String toString() {
		return "LogicalComponentComposer{" +
				"type=" + type +
				", componentId=" + componentId +
				", inputPorts=" + inputPorts +
				", delay=" + delay +
				'}';
	}
}
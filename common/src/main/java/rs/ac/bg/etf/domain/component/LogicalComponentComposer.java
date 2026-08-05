package rs.ac.bg.etf.domain.component;

/**
 * Class that acts as the composer of logical component parts.
 */
public final class LogicalComponentComposer {
	private static final int EXPECTED_NUMBER_OF_ENTRIES = 3;

	private final LogicalGateTypes type;
	private final ComponentId componentId;
	private final long delay;

	private LogicalComponentComposer(LogicalGateTypes type, ComponentId componentId, long delay) {
		this.type = type;
		this.componentId = componentId;
		this.delay = delay;
	}

	public static LogicalComponentComposer parse(String[] data) {
		return ComponentParser.parse(
				data,
				EXPECTED_NUMBER_OF_ENTRIES,
				d -> new LogicalComponentComposer(
						Enum.valueOf(LogicalGateTypes.class, d[0]),
						new ComponentId(d[1]),
						Long.parseLong(d[2])
				));
	}

	public LogicalGateTypes type() {
		return type;
	}

	public ComponentId componentId() {
		return componentId;
	}

	public long delay() {
		return delay;
	}

	@Override
	public String toString() {
		return "LogicalComponentComposer{" +
				"type=" + type +
				", componentId=" + componentId +
				", delay=" + delay +
				'}';
	}
}
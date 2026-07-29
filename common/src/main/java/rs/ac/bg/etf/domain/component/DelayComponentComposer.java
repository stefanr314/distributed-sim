package rs.ac.bg.etf.domain.component;

import rs.ac.bg.etf.domain.exceptions.MalformedComponentDataException;

/**
 * Class {@code DelayComponentComposer} parses tokenized component data into the fields needed to
 * construct a {@link DelayOnlyComponent}: component id and propagation delay.
 *
 * @author stefanr
 * @since 1.0
 */
public final class DelayComponentComposer {
	private static final int EXPECTED_NUMBER_OF_ENTRIES = 2;

	private final ComponentId componentId;
	private final long delay;


	private DelayComponentComposer(ComponentId componentId, long delay) {
		this.componentId = componentId;
		this.delay = delay;
	}

	/**
	 * @param data tokenized data in the format {@code [componentId, delay]}
	 * @return a composer holding the parsed fields
	 * @throws MalformedComponentDataException if {@code data} is null, too short, or contains an unparsable delay value
	 */
	public static DelayComponentComposer parse(String[] data) {
		return ComponentParser.parse(
				data,
				EXPECTED_NUMBER_OF_ENTRIES,
				d -> new DelayComponentComposer(
						new ComponentId(d[0]),
						Long.parseLong(d[1])
				));
	}

	public ComponentId componentId() {
		return componentId;
	}


	public long delay() {
		return delay;
	}

	@Override
	public String toString() {
		return "DelayComponentComposer{" +
				", componentId=" + componentId +
				", delay=" + delay +
				'}';
	}
}
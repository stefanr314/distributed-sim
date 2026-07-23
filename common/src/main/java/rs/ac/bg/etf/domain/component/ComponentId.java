package rs.ac.bg.etf.domain.component;

import java.util.Objects;

/**
 * Class that just serves as the wrapper around the component id, that will be parsed from configuration file
 * provided by the client. It relies on the fact that the client will make components distinct by this value. No UUID
 * wrapper provided for this id due to fact that this component will be used within the single netlist, used on
 * single workstation commanded by the single client request.
 *
 * @author stefanr
 * @since 1.0
 */
public final class ComponentId {
	private final String componentId;
	private final int hash;

	public ComponentId(String clientComponentId) {
		this.componentId = clientComponentId;
		// not the best performance ever seen, but it's called only upon initialization.
		this.hash = Objects.hashCode(clientComponentId);
	}

	public String componentId() {
		return componentId;
	}

	@Override
	public boolean equals(Object o) {
		if (o == this) return true; //check whether the argument is actually the reference to this object

		// check for is instance of and do the casting; otherwise just return false
		if (o instanceof ComponentId that) {

			// do the recursive equals checks for significant fields
			return this.componentId.equals(that.componentId);
		}

		return false;
	}

	@Override
	public int hashCode() {
		return hash;
	}

	@Override
	public String toString() {
		return "ComponentId{" +
				"componentId=" + componentId +
				'}';
	}
}
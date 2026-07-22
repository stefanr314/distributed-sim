package rs.ac.bg.etf.domain.component;

import java.util.Locale;
import java.util.Objects;
import java.util.UUID;


public final class ComponentId {
	private final String componentId;
	private final int hash;

	private ComponentId(String raw) {
		this.componentId = raw;
		// not the best performance ever seen, but it's called only upon initialization.
		this.hash = Objects.hashCode(raw);
	}

	public static ComponentId nextIdentity() {
		return new ComponentId(UUID.randomUUID().toString().toUpperCase(Locale.ROOT));
	}

	public String componentId() {
		return componentId;
	}

	// There is no need for this class to implement the equals since there is no pure logical equality context to it
	// simply the reference distinguishes the two.
//	@Override
//	public boolean equals(Object o) {
//		if (o == this) return true; //check whether the argument is actually the reference to this object
//		if (o == null) return false; //return false for null
//
//		// check for is instance of and do the casting; otherwise just return false
//		if (o instanceof ComponentId that) {
//
//			// do the recursive equals checks for significant fields
//			return this.componentId.equals(that.componentId);
//		}
//
//		return false;
//	}

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
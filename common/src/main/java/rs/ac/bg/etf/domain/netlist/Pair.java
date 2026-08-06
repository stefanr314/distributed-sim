package rs.ac.bg.etf.domain.netlist;

import java.io.Serial;
import java.io.Serializable;

public final class Pair<K extends Serializable, V extends Serializable> implements Serializable {
	@Serial
	private static final long serialVersionUID = 8L;

	private final K id;
	private final V value;

	public Pair(K key, V value) {
		this.id = key;
		this.value = value;
	}

	public K id() {
		return id;
	}

	public V value() {
		return value;
	}

	@Override
	public String toString() {
		return "Pair{" +
				"id=" + id +
				", value=" + value +
				'}';
	}
}
package rs.ac.bg.etf.domain.netlist;

public final class Pair<K, V> {
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
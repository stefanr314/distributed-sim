package rs.ac.bg.etf.domain;

import rs.ac.bg.etf.domain.component.ComponentId;
import rs.ac.bg.etf.domain.event.Event;

import java.io.Serializable;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Class {@code SimpleWorkstationRouting<V>} is the single, shared lookup table a workstation uses to
 * find where each component's inbox lives — one entry per component currently running somewhere on
 * this workstation, regardless of which {@link Simulator}/partition it belongs to. Every
 * {@link InMemorySimBuffer} on the workstation holds a reference to the same instance, so any simulator
 * can deliver to any other simulator's component without knowing anything about network transport.
 * <p>
 * Backed by {@link ConcurrentHashMap}: safe for concurrent {@link #inboxAtComponent(ComponentId)}
 * lookups from every simulator's thread alongside {@link #register}/{@link #unregister} calls as jobs
 * start and finish. This class itself needs no further synchronization, since thread safety is delegated to thread
 * safe class.
 * <p>
 * A single lookup is atomic and therefore safe on its own. If this class ever grows a compound
 * operation (e.g. "register only if absent"), build it on {@link ConcurrentHashMap#putIfAbsent} /
 * {@link ConcurrentHashMap#computeIfAbsent} — composing two separate calls reintroduces a check-then-act
 * race regardless of what backs the map. {@link ComponentId} is immutable, so it is not at risk of the
 * classic mutated-hash-key hazard — worth remembering for any future key type that isn't.
 *
 * @param <V> the value type carried by events routed through this table
 * @author stefanr
 * @since 1.0
 */
public class SimpleWorkstationRouting<V extends Serializable> {
	private final Map<ComponentId, BlockingQueue<Event<V>>> routing;

	/**
	 * @param routing initial component-to-inbox mapping, copied into an internal
	 *                {@link ConcurrentHashMap} — the caller's map (and its own thread-safety or lack
	 *                thereof) is irrelevant beyond this call; mutate only the returned instance
	 *                afterwards, never the original map passed in.
	 */
	public SimpleWorkstationRouting(Map<ComponentId, BlockingQueue<Event<V>>> routing) {
		this.routing = new ConcurrentHashMap<>(routing);
	}

	/**
	 * Thread safety is delegated to concurrency hash map. ID value serves as an immutable object, even if shared no
	 * explicit synchronization is required to check for non-nullness.
	 *
	 * @param id id of the destination component
	 * @return the inbox of the simulator currently hosting {@code id}, or {@code null} if none is
	 * currently registered
	 */
	public BlockingQueue<Event<V>> inboxAtComponent(ComponentId id) {
		Objects.requireNonNull(id);

		return routing.get(id);
	}

	/**
	 * Registers {@code inbox} as the delivery point for events addressed to {@code id}.
	 */
	public void register(ComponentId id, BlockingQueue<Event<V>> inbox) {
		routing.put(id, inbox);
	}

	/**
	 * Removes the routing entry for {@code id} once the job/partition hosting it finishes.
	 */
	public void unregister(ComponentId id) {
		routing.remove(id);
	}
}
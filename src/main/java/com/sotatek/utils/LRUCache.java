package com.sotatek.utils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class LRUCache<K, V extends Comparable<V>> {
	private final int capacity;
	private final int topCapacity;
	private final Map<K, V> cache;
	private final PriorityQueue<CacheEntry<K>> priorityQueue;
	private final SortedMap<V, K> sortedCache;
	private final AtomicInteger hits = new AtomicInteger(0);
	private Consumer<V> listener;

	protected LRUCache(int capacity, int topCapacity) {
		this.capacity = capacity;
		this.topCapacity = topCapacity;
		this.cache = new HashMap<>(capacity);
		this.priorityQueue = new PriorityQueue<>(capacity,
				(first, second) -> first.accessTime.compareTo(second.accessTime));
		this.sortedCache = new TreeMap<>((o1, o2) -> o2.compareTo(o1));
	}

	public V get(K key) {
		if (cache.containsKey(key)) {
			// Update access time
			CacheEntry<K> newAccessTime = new CacheEntry<>(key);
			priorityQueue.remove(newAccessTime);
			priorityQueue.offer(newAccessTime);
			hits.incrementAndGet();
			return cache.get(key);
		}
		return null; // Key not found
	}

	public void put(K key, V value) {
		V evictedValue = null;
		// Put new key-value pair
		if (cache.containsKey(key)) {
			V currentValue = cache.get(key);
			synchronized (currentValue) {
				updateAccount(currentValue, value);
			}
		} else {
			if (cache.size() >= capacity) {
				// Evict the least recently used key
				CacheEntry<K> evicted = priorityQueue.poll();
				if (evicted != null) {
					evictedValue = cache.remove(evicted.key);
				}
			}
			cache.put(key, value);
			priorityQueue.offer(new CacheEntry<>(key));
		}
		// Put new key-value pair to sorted map
		this.putInSortedMap(key, value, evictedValue);
		listener.accept(value);
	}

	private void putInSortedMap(K key, V value, V evictedValue) {
		if (sortedCache.containsKey(value)) {
			sortedCache.put(value, key);
			return;
		}

		if (evictedValue != null) {
			sortedCache.remove(evictedValue);
		}

		V lowestValue;
		try {
			lowestValue = sortedCache.lastKey();
		} catch (Exception e) {
			lowestValue = null;
		}

		if (sortedCache.size() >= topCapacity) {
			if (value.compareTo(lowestValue) >= 0) {
				sortedCache.remove(lowestValue);
				sortedCache.put(value, key);
			}

			return;
		}

		sortedCache.put(value, key);
	}

	public List<V> getTopCacheElements(int numberOfElements) {
		if (numberOfElements > topCapacity) {
			throw new IllegalArgumentException("The number of elements larger than the capacity");
		}
		return sortedCache.keySet().stream().limit(numberOfElements).collect(Collectors.toList());
	}

	public int getHits() {
		return this.hits.get();
	}

	public void delegateListener(Consumer<V> listener) {
		this.listener = listener;
	}

	private static class CacheEntry<K> {
		K key;
		Instant accessTime;

		CacheEntry(K key) {
			this.key = key;
			this.accessTime = Instant.now();
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((key == null) ? 0 : key.hashCode());
			return result;
		}

		@Override
		@SuppressWarnings("unchecked")
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			CacheEntry<K> other = (CacheEntry<K>) obj;

			if (key == null) {
				if (other.key != null)
					return false;
			} else if (!key.equals(other.key))
				return false;

			return true;
		}
	}

	abstract void updateAccount(V currentValue, V newValue);
}

package com.sotatek.utils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class LRUCache<K, V extends Comparable<V>> {
	private final int capacity;
	private final int topCapacity;
	private final Map<K, V> cache;
	private final AtomicReference<PriorityQueue<CacheEntry<K>>> priorityQueue;
	private final AtomicReference<SortedMap<V, K>> sortedCache;
	private final AtomicInteger hits = new AtomicInteger(0);
	private Consumer<V> listener;

	protected LRUCache(int capacity, int topCapacity) {
		this.capacity = capacity;
		this.topCapacity = topCapacity;
		this.cache = new ConcurrentHashMap<>(capacity);
		this.priorityQueue = new AtomicReference<>(new PriorityQueue<>(capacity,
				(first, second) -> first.accessTime.compareTo(second.accessTime)));
		this.sortedCache = new AtomicReference<>(new TreeMap<>((o1, o2) -> o2.compareTo(o1)));
	}

	public V get(K key) {
		if (cache.containsKey(key)) {
			// Update access time
			CacheEntry<K> newAccessTime = new CacheEntry<>(key);
			priorityQueue.get().remove(newAccessTime);
			priorityQueue.get().offer(newAccessTime);
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
				CacheEntry<K> evicted = priorityQueue.get().poll();
				if (evicted != null) {
					evictedValue = cache.remove(evicted.key);
				}
			}
			cache.put(key, value);
			priorityQueue.get().offer(new CacheEntry<>(key));
		}
		// Put new key-value pair to sorted map
		this.putInSortedMap(key, value, evictedValue);
		listener.accept(value);
	}

	private void putInSortedMap(K key, V value, V evictedValue) {
		K changedValue = sortedCache.get().computeIfPresent(value, (k, v) -> key);
		if (changedValue != null) {
			return;
		}

		if (evictedValue != null) {
			sortedCache.get().remove(evictedValue);
		}

		V lowestValue;
		try {
			lowestValue = sortedCache.get().lastKey();
		} catch (Exception e) {
			lowestValue = null;
		}

		if (sortedCache.get().size() >= topCapacity) {
			if (value.compareTo(lowestValue) >= 0) {
				sortedCache.get().remove(lowestValue);
				sortedCache.get().put(value, key);
			}

			return;
		}

		sortedCache.get().put(value, key);
	}

	public List<V> getTopCacheElements(int numberOfElements) {
		if (numberOfElements > topCapacity) {
			throw new IllegalArgumentException("The number of elements larger than the capacity");
		}
		return sortedCache.get().keySet().stream().limit(numberOfElements).collect(Collectors.toList());
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

package com.sotatek.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public abstract class LRUCache<K, V extends Comparable<V>> {
	private final int capacity;
	private final int topCapacity;
	private final Map<K, V> cache;
	private final AtomicReference<SortedMap<V, K>> sortedCache;
	private final AtomicInteger hits = new AtomicInteger(0);
	private Consumer<V> listener;

	protected LRUCache(int capacity, int topCapacity) {
		this.capacity = capacity;
		this.topCapacity = topCapacity;
		this.cache = Collections.synchronizedMap(new LinkedHashMap<>(this.capacity) {
			@Override
			protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
				boolean mustEvictTheEldest = size() > capacity;
				if (mustEvictTheEldest) {
					sortedCache.get().remove(eldest.getValue());
				}
				return mustEvictTheEldest;
			}
		});
		this.sortedCache = new AtomicReference<>(new TreeMap<>((o1, o2) -> o2.compareTo(o1)));
	}

	public V get(K key) {
		if (cache.containsKey(key)) {
			hits.incrementAndGet();
			return cache.get(key);
		}
		return null; // Key not found
	}

	public void put(K key, V value) {
		// Put new key-value pair
		if (cache.containsKey(key)) {
			V currentValue = cache.get(key);
			synchronized (currentValue) {
				updateAccount(currentValue, value);
			}
		} else {
			cache.put(key, value);
		}
		// Put new key-value pair to sorted map
		this.putInSortedMap(key, value);
		listener.accept(value);
	}

	private void putInSortedMap(K key, V value) {
		K changedValue = sortedCache.get().computeIfPresent(value, (k, v) -> key);
		if (changedValue != null) {
			return;
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

	abstract void updateAccount(V currentValue, V newValue);
}

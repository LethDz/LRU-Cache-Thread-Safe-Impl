package com.sotatek.utils;

import com.sotatek.model.Account;

public class LRUAccountCache extends LRUCache<Long, Account> {

	public LRUAccountCache(int capacity, int topCapacity) {
		super(capacity, topCapacity);
	}

	@Override
	void updateAccount(Account currentValue, Account newValue) {
		currentValue.setBalance(newValue.getBalance());
	}
}

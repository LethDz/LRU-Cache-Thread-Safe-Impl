package com.sotatek.service.impl;

import java.util.List;
import java.util.function.Consumer;

import com.sotatek.model.Account;
import com.sotatek.service.AccountCache;
import com.sotatek.utils.LRUAccountCache;

public class AccountCacheImpl implements AccountCache {

	private LRUAccountCache accountCache;

	/**
	 * @param accountCache
	 */
	public AccountCacheImpl(int capacity, int topCapacity) {
		this.accountCache = new LRUAccountCache(capacity, topCapacity);
	}

	@Override
	public Account getAccountById(long id) {
		Long boxedId = Long.valueOf(id);
		return accountCache.get(boxedId);
	}

	@Override
	public void subscribeForAccountUpdates(Consumer<Account> listener) {
		accountCache.delegateListener(listener);
	}

	@Override
	public List<Account> getTop3AccountsByBalance() {
		return accountCache.getTopCacheElements(3);
	}

	@Override
	public int getAccountByIdHitCount() {
		return accountCache.getHits();
	}

	@Override
	public void putAccount(Account account) {
		accountCache.put(account.getId(), account);
	}
}

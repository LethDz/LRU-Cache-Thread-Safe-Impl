package com.sotatek.service;

import java.util.List;
import java.util.function.Consumer;

import com.sotatek.model.Account;

/**
 * Keeps track of accounts in memory, providing methods to query the
 * accounts by different criteria.
 * 
 * @author sotatek
 */
public interface AccountCache {

	/**
	 * @return account by its unique id
	 */
	Account getAccountById(long id);

	/**
	 * Registers a listener that will be notified when an account is
	 * registered or updated via the {@link #putAccount(Account)}
	 * method
	 */
	void subscribeForAccountUpdates(Consumer<Account> listener);

	/**
	 * @return top 3 accounts by balance, sorted from the largest balance
	 *         to the smallest one
	 */
	List<Account> getTop3AccountsByBalance();

	/**
	 * @return the number of 'hits' (when an account was found) to
	 *         the 'getAccountById' method of this service
	 */
	int getAccountByIdHitCount();

	/**
	 * Puts or updates an account in the service. If the cache is at
	 * the full capacity, removes the least recently used account
	 * (LRU policy). 'Used' means either registered by this method
	 * or queried by the {@link #getAccountById(long)} method.
	 */
	void putAccount(Account account);
}

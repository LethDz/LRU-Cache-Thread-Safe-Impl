import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.gen5.api.AfterEach;
import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;

import com.sotatek.model.Account;
import com.sotatek.service.AccountCache;
import com.sotatek.service.impl.AccountCacheImpl;

public class LRUAccountTest {

	private static final List<String> notificationList = new LinkedList<>();
	private ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
	private AccountCache accountCache;

	@AfterEach
	void afterEachTest() {
		notificationList.clear();
	}

	@BeforeEach
	void beforeEachTest() {
		final int cacheCapacity = 4;
		final int topCapacity = 3;
		accountCache = new AccountCacheImpl(cacheCapacity, topCapacity);
		accountCache.subscribeForAccountUpdates(LRUAccountTest::pushNotification);
	}

	@Test
	void runPutAndGetConcurrently() throws Exception {
		Account account1 = new Account(1l, 10l);
		Account account2 = new Account(2l, 10l);
		Account account3 = new Account(3l, 10l);
		Account account4 = new Account(4l, 10l);
		Account account5 = new Account(5l, 10l);
		Account account6 = new Account(6l, 10l);
		Account account7 = new Account(7l, 10l);
		Account account8 = new Account(8l, 10l);
		Account account9 = new Account(9l, 10l);
		Account account10 = new Account(10l, 10l);
		Account account11 = new Account(11l, 10l);

	}

	private static void pushNotification(Account account) {
		String notificationString = String.format("Put account with id: %s and balance: %s", account.getId(),
				account.getBalance());
		notificationList.add(notificationString);
	}
}

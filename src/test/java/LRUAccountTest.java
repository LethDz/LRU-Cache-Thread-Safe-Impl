import java.math.BigDecimal;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

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
		final int cacheCapacity = 10;
		final int topCapacity = 3;
		accountCache = new AccountCacheImpl(cacheCapacity, topCapacity);
		accountCache.subscribeForAccountUpdates(LRUAccountTest::pushNotification);
	}

	@Test
	void runPutAndGetConcurrently() throws Exception {

	}

	private static void pushNotification(Account account) {
		String notificationString = String.format("Put account with id: %s and balance: %s", account.getId(),
				account.getBalance());
		notificationList.add(notificationString);
	}
}

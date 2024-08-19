package ua.com.pragmasoft.ratelimiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import ua.com.pragmasoft.ratelimiter.exception.RateLimitExceededException;
import ua.com.pragmasoft.ratelimiter.token_bucket.TokenBucketImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class TokenBucketImplTest {
    private TokenBucketImpl bucket;

    @BeforeEach
    void setUp() {
        bucket = new TokenBucketImpl(10, 5);
    }

    @Test
    void testGetToken_Success() throws RateLimitExceededException {
        bucket.getToken(5);
        assertDoesNotThrow(() -> bucket.getToken(3));
    }

    @Test
    void testGetToken_ExceedsLimit() throws RateLimitExceededException {
        bucket.getToken(10);
        assertThrows(RateLimitExceededException.class, () -> bucket.getToken(1));
    }

    @Test
    void testRefillTokens() throws InterruptedException, RateLimitExceededException {
        bucket.getToken(5);
        TimeUnit.SECONDS.sleep(1);
        bucket.getToken(5);
        assertDoesNotThrow(() -> bucket.getToken(1));
    }

    @Test
    void testCalculateRetryAfter_RefillRateZero() throws RateLimitExceededException {
        TokenBucketImpl bucket = new TokenBucketImpl(10, 0);
        bucket.getToken(10);
        assertThrows(IllegalStateException.class, bucket::calculateRetryAfter);
    }

    @Test
    @Timeout(10)
    void testConcurrentTokenRequests() throws InterruptedException {
        long size = 10;
        long refillRate = 5;
        TokenBucketImpl bucket = new TokenBucketImpl(size, refillRate);
        int numThreads = 10;
        int tokensPerRequest = 1;
        AtomicInteger successfulRequests = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(numThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    if (bucket.getToken(tokensPerRequest)) {
                        successfulRequests.incrementAndGet();
                    }
                } catch (RateLimitExceededException e) {
                    System.out.println("This exception may not be thrown");
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertTrue(successfulRequests.get() > 0, "Some requests should have succeeded");
    }

    @Test
    @Timeout(10)
    void testRateLimitExceededExceptionInMultithreadedEnvironment() throws InterruptedException {

        long size = 5;
        long refillRate = 10;
        TokenBucketImpl bucket = new TokenBucketImpl(size, refillRate);
        int numThreads = 10;
        int tokensPerRequest = 1;
        AtomicInteger exceededCount = new AtomicInteger(0);
        CountDownLatch latch = new CountDownLatch(numThreads);
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);

        // When
        for (int i = 0; i < numThreads; i++) {
            executor.submit(() -> {
                try {
                    bucket.getToken(tokensPerRequest);
                } catch (RateLimitExceededException e) {
                    System.out.println(exceededCount);
                    exceededCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        assertTrue(exceededCount.get() > 0, "At least one request should have exceeded the rate limit");
    }
}
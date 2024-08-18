package ua.com.pragmasoft.ratelimiter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.com.pragmasoft.ratelimiter.exception.RateLimitExceededException;
import ua.com.pragmasoft.ratelimiter.token_bucket.TokenBucket;
import ua.com.pragmasoft.ratelimiter.token_bucket.TokenBucketImpl;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        // Should be able to get 5 tokens without exception
        assertDoesNotThrow(() -> bucket.getToken(3));
    }

    @Test
    void testGetToken_ExceedsLimit() throws RateLimitExceededException {
        bucket.getToken(10);
        // Should throw RateLimitExceededException as we have no tokens left
        assertThrows(RateLimitExceededException.class, () -> bucket.getToken(1));
    }

    @Test
    void testRefillTokens() throws InterruptedException, RateLimitExceededException {
        bucket.getToken(5);
        TimeUnit.SECONDS.sleep(1); // wait for 1 second
        bucket.getToken(5); // should succeed as tokens should be refilled
        assertDoesNotThrow(() -> bucket.getToken(1));
    }

    @Test
    void testCalculateRetryAfter_RefillRateZero() throws RateLimitExceededException {
        // Устанавливаем refillRate в 0
        TokenBucketImpl bucket = new TokenBucketImpl(10, 0); // refillRate 0
        bucket.getToken(10); // вызовет исключение

        assertThrows(IllegalStateException.class, bucket::calculateRetryAfter);
    }

    @Test
    public void testTokenBucketWithConcurrentAccess() throws InterruptedException {
        final int threadCount = 10;
        final int tokensPerThread = 1;
        final int bucketSize = 5;
        final int refillRate = 10;

        TokenBucket bucket = new TokenBucketImpl(bucketSize, refillRate);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    boolean success = bucket.getToken(tokensPerThread);
                    assertTrue(success);
                } catch (RateLimitExceededException e) {
                    System.out.println("This exception may not be thrown");
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean latchCompleted = latch.await(10, TimeUnit.SECONDS);
        assertTrue(latchCompleted,"Tasks did not complete in time" );

        executor.shutdown();
        boolean executorTerminated = executor.awaitTermination(10, TimeUnit.SECONDS);
        assertTrue(executorTerminated,"Executor did not terminate in time" );
    }
    @Test
    public void testTokenBucketWithRateLimitExceeded() throws InterruptedException {
        final int threadCount = 10;
        final int tokensPerThread = 1;
        final int bucketSize = 5;
        final int refillRate = 10; // tokens per second

        TokenBucket bucket = new TokenBucketImpl(bucketSize, refillRate);
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    if (!bucket.getToken(tokensPerThread)) {
                        throw new AssertionError("Expected RateLimitExceededException");
                    }
                } catch (RateLimitExceededException e) {
                    // Expected exception
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean latchCompleted = latch.await(10, TimeUnit.SECONDS);
        assertTrue(latchCompleted,"Tasks did not complete in time" );

        executor.shutdown();
        boolean executorTerminated = executor.awaitTermination(10, TimeUnit.SECONDS);
        assertTrue(executorTerminated,"Executor did not terminate in time" );
    }
}

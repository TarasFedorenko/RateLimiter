package ua.com.pragmasoft.ratelimiter;

import org.junit.jupiter.api.Test;
import ua.com.pragmasoft.ratelimiter.token_bucket.TokenBucket;
import ua.com.pragmasoft.ratelimiter.token_bucket.TokenBucketImpl;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TokenBucketImplTest {

    @Test
    public void testGetToken_Success() {
        TokenBucket tokenBucket = new TokenBucketImpl(5, 10);
        assertTrue(tokenBucket.getToken(3));
    }

    @Test
    public void testGetToken_Failure() {
        TokenBucket tokenBucket = new TokenBucketImpl(2, 10);
        assertFalse(tokenBucket.getToken(3));
    }

    @Test
    public void testTokenRefill() throws InterruptedException {
        TokenBucketImpl tokenBucket = new TokenBucketImpl(5, 1);
        assertTrue(tokenBucket.getToken(5));
        Thread.sleep(1000); // Wait for 1 second
        assertTrue(tokenBucket.getToken(1));
        assertFalse(tokenBucket.getToken(2));
    }
}

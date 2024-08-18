package ua.com.pragmasoft.ratelimiter.token_bucket;

import ua.com.pragmasoft.ratelimiter.exception.RateLimitExceededException;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of the {@link TokenBucket} interface using an atomic counter for tokens and a refill mechanism.
 */
public class TokenBucketImpl implements TokenBucket {
    private final long size;
    private final long refillRate;
    private final AtomicLong tokens;
    private final AtomicLong lastRefillTimestamp;

    private static final ConcurrentMap<String, TokenBucketImpl> buckets = new ConcurrentHashMap<>();
    private static final long CLEANUP_INTERVAL_MS = 60000; // Cleanup interval in milliseconds

    /**
     * Constructs a {@link TokenBucketImpl} with the specified size and refill rate.
     *
     * @param size       the maximum number of tokens in the bucket
     * @param refillRate the rate at which tokens are added (tokens per second)
     */
    public TokenBucketImpl(long size, long refillRate) {
        this.size = size;
        this.refillRate = refillRate;
        this.tokens = new AtomicLong(size);
        this.lastRefillTimestamp = new AtomicLong(System.currentTimeMillis());
    }

    /**
     * Attempts to retrieve a specified number of tokens from the bucket.
     *
     * @param tokensForBucket the number of tokens to retrieve
     * @throws RateLimitExceededException if the tokens cannot be retrieved
     */
    @Override
    public boolean getToken(int tokensForBucket) throws RateLimitExceededException {
        refill();
        long currentTokens = tokens.get();
        if (currentTokens >= tokensForBucket) {
            tokens.addAndGet(-tokensForBucket);
            return true;
        } else {
            long retryAfterMillis = calculateRetryAfter();
            String errorReason = "Exceeded the allowed rate limit.";
            throw new RateLimitExceededException("Rate limit exceeded. Try again later.", retryAfterMillis, errorReason);
        }
    }

    /**
     * Refills the bucket with tokens based on the elapsed time since the last refill.
     */
    private synchronized void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTimestamp.get();
        if (elapsed > CLEANUP_INTERVAL_MS) {
            // Perform cleanup if necessary
            cleanUpBuckets();
        }
        long newTokens = elapsed * refillRate / 1000;
        tokens.set(Math.min(size, tokens.get() + newTokens));
        lastRefillTimestamp.set(now);
    }

    /**
     * Calculates the time in milliseconds until the next available token.
     *
     * @return the time to wait in milliseconds
     */
    public long calculateRetryAfter() {
        long currentTokens = tokens.get();
        long tokensNeeded = size - currentTokens;
        if (refillRate <= 0) {
            throw new IllegalStateException("Refill rate must be greater than zero");
        }
        return tokensNeeded * 1000 / refillRate;
    }

    /**
     * Retrieves or creates a token bucket for the specified client key.
     *
     * @param clientKey  the unique client key
     * @param size       the maximum number of tokens in the bucket
     * @param refillRate the rate at which tokens are added (tokens per second)
     * @return the token bucket for the specified client key
     */
    public static TokenBucket getBucket(String clientKey, long size, long refillRate) {
        return buckets.compute(clientKey, (k, existingBucket) -> {
            return Objects.requireNonNullElseGet(existingBucket, () -> new TokenBucketImpl(size, refillRate));
        });
    }

    /**
     * Performs cleanup of expired buckets to avoid memory leaks.
     */
    private static synchronized void cleanUpBuckets() {
        long now = System.currentTimeMillis();
        buckets.forEach((key, bucket) -> {
            if (bucket.isExpired(now)) {
                buckets.remove(key);
            }
        });
    }

    /**
     * Determines if the bucket has expired.
     *
     * @param currentTime the current time in milliseconds
     * @return true if the bucket has expired, false otherwise
     */
    private boolean isExpired(long currentTime) {
        // Example expiration logic; customize as needed
        return currentTime - lastRefillTimestamp.get() > CLEANUP_INTERVAL_MS;
    }
}

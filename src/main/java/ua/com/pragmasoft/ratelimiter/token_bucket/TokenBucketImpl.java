package ua.com.pragmasoft.ratelimiter.token_bucket;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Implementation of the {@link TokenBucket} interface using an atomic counter for tokens and refilling mechanism.
 */
public class TokenBucketImpl implements TokenBucket {
    private final long size;
    private final long refillRate;
    private final AtomicLong tokens;
    private final AtomicLong lastRefillTimestamp;

    public static final ConcurrentMap<String, TokenBucket> buckets = new ConcurrentHashMap<>();

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
     * @return true if tokens were successfully retrieved, false otherwise
     */
    @Override
    public boolean getToken(int tokensForBucket) {
        refill();
        long currentTokens = tokens.get();
        if (currentTokens >= tokensForBucket) {
            tokens.addAndGet(-tokensForBucket);
            return true;
        }
        return false;
    }

    /**
     * Refills the bucket with tokens based on elapsed time since the last refill.
     */
    private void refill() {
        long now = System.currentTimeMillis();
        long elapsed = now - lastRefillTimestamp.get();
        long newTokens = elapsed * refillRate / 1000;
        tokens.set(Math.min(size, tokens.get() + newTokens));
        lastRefillTimestamp.set(now);
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
        return buckets.computeIfAbsent(clientKey, k -> new TokenBucketImpl(size, refillRate));
    }
}

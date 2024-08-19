package ua.com.pragmasoft.ratelimiter.token_bucket;

import ua.com.pragmasoft.ratelimiter.exception.RateLimitExceededException;

/**
 * Interface for a token bucket used in rate limiting.
 */
public interface TokenBucket {

    /**
     * Attempts to retrieve a specified number of tokens from the bucket.
     *
     * @param tokensForBucket the number of tokens to retrieve
     * @return true if the tokens were successfully retrieved, false otherwise
     */
    boolean getToken(int tokensForBucket) throws RateLimitExceededException;
}

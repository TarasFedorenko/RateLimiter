package ua.com.pragmasoft.ratelimiter.exception;

/**
 * Exception thrown when a rate limit is exceeded.
 */
public class RateLimitExceededException extends Exception {
    private final long retryAfterMillis;
    private final String errorReason;

    /**
     * Constructs an exception with the specified message, retry time, and error reason.
     *
     * @param message          the detail message
     * @param retryAfterMillis time in milliseconds to wait before retrying
     * @param errorReason      the reason for the rate limit being exceeded
     */
    public RateLimitExceededException(String message, long retryAfterMillis, String errorReason) {
        super(message);
        this.retryAfterMillis = retryAfterMillis;
        this.errorReason = errorReason;
    }

    /**
     * Returns the time in milliseconds to wait before making another request.
     *
     * @return the retry-after time in milliseconds
     */
    public long getRetryAfterMillis() {
        return retryAfterMillis;
    }

    /**
     * Returns the reason for the rate limit being exceeded.
     *
     * @return the reason for the error
     */
    public String getErrorReason() {
        return errorReason;
    }
}

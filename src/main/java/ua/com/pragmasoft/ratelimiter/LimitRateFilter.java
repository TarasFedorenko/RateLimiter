package ua.com.pragmasoft.ratelimiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;
import ua.com.pragmasoft.ratelimiter.client_key.ClientKeyStrategy;
import ua.com.pragmasoft.ratelimiter.token_bucket.TokenBucket;
import ua.com.pragmasoft.ratelimiter.token_bucket.TokenBucketImpl;

import java.io.IOException;

/**
 * Servlet filter that applies rate limiting based on the token bucket algorithm.
 */
public class LimitRateFilter extends OncePerRequestFilter {

    @Value("${token.size:5}")
    int size;

    @Value("${token.refill:10}")
    int refillRate;

    private final ClientKeyStrategy clientKeyStrategy;

    /**
     * Constructs a {@link LimitRateFilter} with the specified client key strategy.
     *
     * @param clientKeyStrategy the strategy for extracting client keys
     */
    public LimitRateFilter(ClientKeyStrategy clientKeyStrategy) {
        this.clientKeyStrategy = clientKeyStrategy;
    }

    /**
     * Filters the request by checking if the client has enough tokens in the bucket.
     * If tokens are available, the request proceeds. Otherwise, a 429 status is returned.
     *
     * @param request     the HTTP request
     * @param response    the HTTP response
     * @param filterChain the filter chain
     * @throws IOException      if an I/O error occurs
     * @throws ServletException if a servlet error occurs
     */
    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain) throws ServletException, IOException {
        String clientKey = clientKeyStrategy.getClientKey(request);
        TokenBucket bucket = TokenBucketImpl.getBucket(clientKey, size, refillRate);

        if (bucket.getToken(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.getWriter().write("Rate limit exceeded");
            response.setStatus(429);
        }
    }
}

package ua.com.pragmasoft.ratelimiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import ua.com.pragmasoft.ratelimiter.client_key.ClientKeyStrategy;
import ua.com.pragmasoft.ratelimiter.exception.RateLimitExceededException;
import ua.com.pragmasoft.ratelimiter.token_bucket.TokenBucket;

import java.io.IOException;
import java.io.PrintWriter;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class LimitRateFilterTest {

    @InjectMocks
    private LimitRateFilter limitRateFilter;

    @Mock
    private ClientKeyStrategy clientKeyStrategy;

    @Mock
    private TokenBucket tokenBucket;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private FilterChain filterChain;

    @Mock
    private PrintWriter writer;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        when(response.getWriter()).thenReturn(writer);
        when(clientKeyStrategy.getClientKey(request)).thenReturn("clientKey");
        limitRateFilter.size = 5;
        limitRateFilter.refillRate = 10;
    }

    @Test
    void testDoFilterInternal_Success() throws Exception {
        when(tokenBucket.getToken(1)).thenReturn(true);
        limitRateFilter.doFilterInternal(request, response, filterChain);
        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
    }
    @Test
    void testGetTokenThrowsRateLimitExceededException() throws RateLimitExceededException {
        doThrow(new RateLimitExceededException("Rate limit exceeded. Try again later.", 1000L, "Exceeded the allowed rate limit."))
                .when(tokenBucket).getToken(1);
        assertThrows(RateLimitExceededException.class, () -> tokenBucket.getToken(1));
    }
}


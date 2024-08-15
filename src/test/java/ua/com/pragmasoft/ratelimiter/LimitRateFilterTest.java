package ua.com.pragmasoft.ratelimiter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.com.pragmasoft.ratelimiter.client_key.IPClientKeyStrategy;
import ua.com.pragmasoft.ratelimiter.token_bucket.TokenBucket;
import ua.com.pragmasoft.ratelimiter.token_bucket.TokenBucketImpl;

import java.io.IOException;
import java.io.PrintWriter;

import static org.mockito.Mockito.*;

public class LimitRateFilterTest {

    private LimitRateFilter limitRateFilter;
    private IPClientKeyStrategy ipClientKeyStrategy;
    private TokenBucket tokenBucket;
    private PrintWriter printWriter;

    @BeforeEach
    public void setUp() {
        ipClientKeyStrategy = mock(IPClientKeyStrategy.class);
        tokenBucket = mock(TokenBucket.class);
        printWriter = mock(PrintWriter.class);

        limitRateFilter = new LimitRateFilter(ipClientKeyStrategy);
        limitRateFilter.size = 5;
        limitRateFilter.refillRate = 10;
    }

    @Test
    public void testDoFilterInternal_AllowsRequest_WhenTokenAvailable() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(ipClientKeyStrategy.getClientKey(request)).thenReturn("127.0.0.1");
        when(tokenBucket.getToken(1)).thenReturn(true);
        when(response.getWriter()).thenReturn(printWriter);
        TokenBucketImpl.buckets.put("127.0.0.1", tokenBucket);


        limitRateFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        verify(response, never()).setStatus(429);
        verify(response, never()).getWriter();
    }

    @Test
    public void testDoFilterInternal_RejectsRequest_WhenNoTokenAvailable() throws IOException, ServletException {
        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpServletResponse response = mock(HttpServletResponse.class);
        FilterChain filterChain = mock(FilterChain.class);

        when(ipClientKeyStrategy.getClientKey(request)).thenReturn("127.0.0.1");
        when(tokenBucket.getToken(1)).thenReturn(false);
        when(response.getWriter()).thenReturn(printWriter);
        TokenBucketImpl.buckets.put("127.0.0.1", tokenBucket);

        limitRateFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain, never()).doFilter(request, response);
        verify(response).setStatus(429);
        verify(printWriter).write("Rate limit exceeded");
    }
}
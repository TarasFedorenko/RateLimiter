package ua.com.pragmasoft.ratelimiter;

import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import ua.com.pragmasoft.ratelimiter.client_key.IPClientKeyStrategy;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ClientIPExtractorTest {

    private final IPClientKeyStrategy ipClientKeyStrategy = new IPClientKeyStrategy();

    @Test
    public void testGetClientIP_XForwardedFor() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.0.1, 192.168.0.2");
        assertEquals("192.168.0.1", ipClientKeyStrategy.getClientKey(request));
    }

    @Test
    public void testGetClientIP_XForwardedForSingleIP() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn("192.168.0.1");
        assertEquals("192.168.0.1", ipClientKeyStrategy.getClientKey(request));
    }

    @Test
    public void testGetClientIP_XRealIP() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        Mockito.when(request.getHeader("X-Real-IP")).thenReturn("192.168.0.3");
        assertEquals("192.168.0.3", ipClientKeyStrategy.getClientKey(request));
    }

    @Test
    public void testGetClientIP_RemoteAddr() {
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        Mockito.when(request.getHeader("X-Forwarded-For")).thenReturn(null);
        Mockito.when(request.getHeader("X-Real-IP")).thenReturn(null);
        Mockito.when(request.getRemoteAddr()).thenReturn("192.168.0.4");
        assertEquals("192.168.0.4", ipClientKeyStrategy.getClientKey(request));
    }
}
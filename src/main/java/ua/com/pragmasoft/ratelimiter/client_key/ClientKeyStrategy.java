package ua.com.pragmasoft.ratelimiter.client_key;

import jakarta.servlet.http.HttpServletRequest;

/**
 * Strategy interface for extracting a client key from an HTTP request.
 */
public interface ClientKeyStrategy {
    /**
     * Retrieves a unique client key from the given HTTP request.
     *
     * @param request the HTTP request
     * @return the client key
     */
    String getClientKey(HttpServletRequest request);
}
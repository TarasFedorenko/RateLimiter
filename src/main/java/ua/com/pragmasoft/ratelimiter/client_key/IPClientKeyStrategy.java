package ua.com.pragmasoft.ratelimiter.client_key;

import jakarta.servlet.http.HttpServletRequest;


/**
 * Implementation of {@link ClientKeyStrategy} that extracts the client IP address.
 */

public class IPClientKeyStrategy implements ClientKeyStrategy {

    /**
     * Retrieves the client IP address from the given HTTP request.
     * It checks the "X-Forwarded-For" and "X-Real-IP" headers first, falling back to
     * {@link HttpServletRequest#getRemoteAddr()} if the headers are not present.
     *
     * @param httpServletRequest the HTTP request
     * @return the client IP address
     */
    public String getClientKey(HttpServletRequest httpServletRequest) {
        String ipAddress = httpServletRequest.getHeader("X-Forwarded-For");
        if (ipAddress != null && !ipAddress.isEmpty()) {
            int index = ipAddress.indexOf(",");
            if (index > 0) {
                return ipAddress.substring(0, index).trim();
            } else {
                return ipAddress.trim();
            }
        }
        ipAddress = httpServletRequest.getHeader("X-Real-IP");
        if (ipAddress != null && !ipAddress.isEmpty()) {
            return ipAddress.trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}

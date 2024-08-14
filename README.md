# RateLimiterWithoutRedis
Simple rate limiter that should limit the incoming request rate to the configurable number of requests per time period
## Overview

This application implements a rate limiter using the token bucket algorithm. It helps to control the rate of incoming HTTP requests, providing protection against abuse and ensuring fair usage. The application is built with Spring Boot and Jakarta Servlet.

## Components

1. **ClientKeyStrategy**: An interface for extracting client keys from HTTP requests.
2. **IPClientKeyStrategy**: A concrete implementation of `ClientKeyStrategy` that extracts client IP addresses.
3. **LimitRateFilter**: A servlet filter that applies rate limiting based on the token bucket algorithm.
4. **RateLimiterApplication**: The main Spring Boot application class.
5. **TokenBucket**: An interface for token bucket implementations.
6. **TokenBucketImpl**: A concrete implementation of `TokenBucket` that uses an atomic counter for tokens and refills based on elapsed time.

## Setup

1. **Clone the Repository**

   ```bash
   git clone https://github.com/TarasFedorenko/RateLimiterWithoutRedis.git
   
2. **Build the Application**
   mvn clean install

3. **Run the Application**
   mvn spring-boot:run

4. **Configuration**
   The application uses properties defined in application.properties. The following properties can be configured:

token.size: Defines the maximum number of tokens in the bucket. Default is 5.

token.refill: Defines the rate at which tokens are added to the bucket per second. Default is 10.

5. **Usage**
   Rate Limiting

The rate limiter is applied to all incoming HTTP requests. The LimitRateFilter checks if a client has enough 
tokens in their bucket before allowing the request to proceed.

Client Key Extraction

The IPClientKeyStrategy extracts the client IP address from the request headers or falls back to the remote
address. This IP address is used as the client key for rate limiting.

Error Handling

If a client exceeds their rate limit, the filter responds with HTTP status 429 Too Many Requests and a message
"Rate limit exceeded".




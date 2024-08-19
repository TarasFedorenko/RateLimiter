# RateLimiter
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
7. **RateLimitExceededException**: An exception class that thrown when a rate limit is exceeded
8. **HelloController**: A simple controller class to test the application

## Setup

1. **Clone the Repository**

   ```bash
   git clone https://github.com/TarasFedorenko/RateLimiter.git
   
2. **Build the Application**
   mvn clean install

3. **Run the Application**
   mvnw spring-boot:run

4. **Configuration**
   The application uses properties defined in application.properties. The following properties can be configured:

- token.size: Defines the maximum number of tokens in the bucket. Default is 5.

- token.refill: Defines the rate at which tokens are added to the bucket per second. Default is 10.

5. **Usage**
   
*Rate Limiting*

The rate limiter is applied to all incoming HTTP requests. The LimitRateFilter checks if a client has enough 
tokens in their bucket before allowing the request to proceed.

*Client Key Extraction*

The IPClientKeyStrategy extracts the client IP address from the request headers or falls back to the remote
address. This IP address is used as the client key for rate limiting.

*Error Handling*

If a client exceeds their rate limit, the filter responds with HTTP status 429 Too Many Requests and a message
with information about retry time, and error reason.

6. **Proposed Solution for Horizontal Scalability**

To ensure that our rate limiter can scale horizontally and work effectively in a distributed environment, I 
propose the following solution.
- Abstract Storage Interface
     I will define an interface to abstract the storage of token bucket states. This interface allows us to 
     switch between different storage implementations without altering the core rate limiter logic.
- Distributed Storage Implementations
    Redis is a high-performance, in-memory data structure store that is well-suited for distributed applications.
    It supports various data structures and provides persistence options. 
    Database storage can also be used, either SQL or NoSQL, depending on requirements and existing infrastructure.
- Token Bucket Implementation
    The TokenBucketImpl class will be modified to use the TokenBucketStorage interface for managing the state of 
    token buckets. This allows the rate limiter to work across multiple instances by accessing the shared storage.
- Configuration
    The application will be configured to use the chosen TokenBucketStorage implementation. This configuration will 
    be specified in the application's configuration files or setup classes.
- Testing
  Integration tests will be used to ensure that the token bucket state is correctly managed across different instances
  or stateless functions. These tests will validate the consistency and correctness of the distributed storage approach.
  Added tests for the behavior of the algorithm in a multi-threading environment.



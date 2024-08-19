package ua.com.pragmasoft.ratelimiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * A simple controller to test the application
 */
@RestController
@RequestMapping("/")
public class HelloController {

    /**
     * Processes GET requests to the root path and returns a welcome message.
     *
     * @return string with a welcome message
     */
    @GetMapping
    public String sayHello() {
        return "Hello, World!";
    }
}

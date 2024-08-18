package ua.com.pragmasoft.ratelimiter.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Простой контроллер для проверки работы приложения.
 */
@RestController
@RequestMapping("/")
public class HelloController {

    /**
     * Обрабатывает GET-запросы по корневому пути и возвращает приветственное сообщение.
     *
     * @return строку с приветствием
     */
    @GetMapping
    public String sayHello() {
        return "Hello, World!";
    }
}

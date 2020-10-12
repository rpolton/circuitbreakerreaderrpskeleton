package com.example.circuitbreakerreading;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@RestController
public class BookService {
    private final RestTemplate restTemplate;
    private final String bookstoreServer;

    public BookService(@Value("${bookstore.server}") final String bookstoreServer, final RestTemplate restTemplate) {
        this.bookstoreServer = bookstoreServer;
        this.restTemplate = restTemplate;
    }

    @HystrixCommand(fallbackMethod="reliable")
    @RequestMapping("/to-read")
    public String readingList() {
        final URI uri = URI.create(bookstoreServer + "/recommended");

        return "Please read: " + restTemplate.getForObject(uri, String.class);
    }

    private String reliable() {
        return "Please read: Cloud Native Java (O'Reilly) - returned as a fallbackMethod via Hystrix because the Bookstore service is not stable";
    }
}
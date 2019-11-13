//package com.example.circuitbreakerreading.t1;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//import org.springframework.web.client.RestTemplate;
//
//import java.net.URI;
//
//@RestController
//public class BookService {
//
//    private final RestTemplate restTemplate;
//    private final String bookstoreServer;
//
//    public BookService(final RestTemplate rest, @Value("${bookstore.server}") final String bookstoreServer) {
//        this.restTemplate = rest;
//        this.bookstoreServer = bookstoreServer;
//    }
//
//    @RequestMapping("/to-read")
//    public String readingList() {
//        final URI uri = URI.create(bookstoreServer + "/recommended");
//
//        return "Please read: " + this.restTemplate.getForObject(uri, String.class);
//    }
//}

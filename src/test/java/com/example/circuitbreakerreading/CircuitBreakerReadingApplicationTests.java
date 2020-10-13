package com.example.circuitbreakerreading;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CircuitBreakerReadingApplicationTests {

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RestTemplate remoteServiceRestTemplate;

    private MockRestServiceServer remoteServer;

    @BeforeEach
    public void setup() {
        remoteServer = MockRestServiceServer.createServer(remoteServiceRestTemplate);
    }

//    @Test
//    void toRead() {
//        final String books = testRestTemplate.getForObject("/to-read", String.class);
//        assertThat(books).isEqualTo("Please read: Beano");
//    }

    @Test
    void toReadRedirectsToTheRemoteService() {
        remoteServer.expect(requestTo("http://localhost:8090/recommended"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("Beano", MediaType.TEXT_PLAIN));
        final String books = testRestTemplate.getForObject("/to-read", String.class);
        remoteServer.verify();
        assertThat(books).isEqualTo("Please read: Beano");
    }

    @Test
    void toReadFailure() {
        remoteServer.expect(requestTo("http://localhost:8090/recommended"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withServerError());
        final String books = testRestTemplate.getForObject("/to-read", String.class);
        remoteServer.verify();
        assertThat(books).isEqualTo("Please read: Cloud Native Java (O'Reilly) - " +
                "returned as a fallbackMethod via Hystrix because the Bookstore service is not stable");
    }

    @Test
    void toReadShortDelay() {
        remoteServer.expect(requestTo("http://localhost:8090/recommended"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(2));
                    } catch(final InterruptedException ignored) {

                    }
                    return new MockClientHttpResponse("Beano Annual 2020".getBytes(), HttpStatus.OK);
                });
        final String books = testRestTemplate.getForObject("/to-read", String.class);
        remoteServer.verify();
        assertThat(books).isEqualTo("Please read: Beano Annual 2020");
    }

    @Test
    void toReadLongDelay() {
        remoteServer.expect(requestTo("http://localhost:8090/recommended"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(request -> {
                    try {
                        Thread.sleep(TimeUnit.SECONDS.toMillis(10));
                    } catch(final InterruptedException ignored) {

                    }
                    return new MockClientHttpResponse("Beano Annual 2020".getBytes(), HttpStatus.OK);
                });
        final String books = testRestTemplate.getForObject("/to-read", String.class);
        remoteServer.verify();
//        assertThat(books).isEqualTo("Please read: Beano Annual 2020");
        assertThat(books).isEqualTo("Please read: Cloud Native Java (O'Reilly) - " +
                "returned as a fallbackMethod via Hystrix because the Bookstore service is not stable");
    }

    @AfterEach
    public void teardown() {
        remoteServer = null;
    }
}
package com.example.circuitbreakerreading;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
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

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment= SpringBootTest.WebEnvironment.RANDOM_PORT)
class CircuitBreakerReadingApplicationTests {

    private MockRestServiceServer server;

    @Autowired
    private TestRestTemplate testRestTemplate;

    @Autowired
    private RestTemplate rest;

    @BeforeEach
    void setup() {
        server = MockRestServiceServer.createServer(rest);
    }

    @AfterEach
    void teardown() {
        server = null;
    }

    @Nested
    class TimeT1 {
        // Add happy path rest call test (with mock of the call to the bookstore service)
        @Test
        void toReadShouldReturnListOfBooks() {
            server.expect(requestTo("http://localhost:8090/recommended"))
                    .andExpect(method(HttpMethod.GET)).
                    andRespond(withSuccess("Goldfinger", MediaType.TEXT_PLAIN));
            final String books = testRestTemplate.getForObject("/to-read", String.class);
            assertThat(books).isEqualTo("Please read: Goldfinger");
        }
    }

    @Nested
    class TimeT2 {
        // Add failing test - unhandled error on mocked ReST call
        @Test
        void toReadShouldReturnDefaultResponseWhenUnderlyingServiceFails() {
            server.expect(requestTo("http://localhost:8090/recommended")).
                    andExpect(method(HttpMethod.GET)).andRespond(withServerError());
            final String books = testRestTemplate.getForObject("/to-read", String.class);
            assertThat(books).isEqualTo("Please read: Cloud Native Java (O'Reilly) - returned as a fallbackMethod via Hystrix because the Bookstore service is not stable");
        }
    }

    @Nested
    class TimeT3 {
        // add test for short delay which will fail as there is no hystrix config as yet for the timeout value (so it will straight execute the fallback)
        @Test
        void toReadShortDelayTest() {
            server.expect(requestTo("http://localhost:8090/recommended")).
                    andExpect(method(HttpMethod.GET)).andRespond(request -> {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(1));
                } catch (final InterruptedException ignored) {}
                return new MockClientHttpResponse("Goldfinger".getBytes(), HttpStatus.OK);
            });
            final String books = testRestTemplate.getForObject("/to-read", String.class);
            assertThat(books).isEqualTo("Please read: Goldfinger");
        }
    }

    @Nested
    class TimeT4 {
        // add test with long timeout - this will pass as well now, returning the fallback method.
        //
        //	NB - interesting to note whilst the timeout is 10 seconds, because of hystrix config the test finishes in 5 seconds
        @Test
        void toReadDelayTest() {
            server.expect(requestTo("http://localhost:8090/recommended")).
                    andExpect(method(HttpMethod.GET)).andRespond(request -> {
                try {
                    Thread.sleep(TimeUnit.SECONDS.toMillis(10000));
                } catch (final InterruptedException ignored) {}
                return new MockClientHttpResponse("Goldfinger".getBytes(), HttpStatus.OK);
            });
            final String books = testRestTemplate.getForObject("/to-read", String.class);
            assertThat(books).isEqualTo("Please read: Cloud Native Java (O'Reilly) - returned as a fallbackMethod via Hystrix because the Bookstore service is not stable");
        }
    }

    // there we have it - a shiny new TDD built rest service
    // my assistant with the luscious beard will now demonstrate hystrix working from a user perspective
}

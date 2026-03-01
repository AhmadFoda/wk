package com.wolterskluwer.proxy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest
@AutoConfigureWebTestClient
class ProxyApplicationTests {

    @Autowired
    private WebTestClient webTestClient;


    @Test
    void shouldRejectUnauthenticatedRequests() {
        webTestClient.get()
                .uri("/api/v1/organisations")
                .exchange()
                .expectStatus().isUnauthorized();
    }

    @Test
    void shouldHandleCors() {
        webTestClient.get()
                .uri("/api/v1/organisations")
                .header("Origin", "http://localhost:3000")
                .exchange()
                .expectStatus().isForbidden();
    }
}
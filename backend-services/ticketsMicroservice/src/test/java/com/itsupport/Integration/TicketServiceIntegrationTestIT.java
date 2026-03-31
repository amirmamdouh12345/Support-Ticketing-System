package com.itsupport.Integration;

import com.itsupport.repos.TicketRepo;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static io.restassured.RestAssured.given;
import static org.junit.jupiter.api.Assertions.assertTrue;
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
public class TicketServiceIntegrationTestIT {

    @Container
    @ServiceConnection
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:latest"); // Specific version is better

    @Autowired
    private TicketRepo ticketRepository; // To verify DB state

    @LocalServerPort
    private Integer port;

//    @BeforeEach
//    void setUp() {
//        ticketRepository.deleteAll(); // Clean state before each test
//        RestAssured.baseURI = "http://localhost:" + port;
//    }

    @Test
    void testCreateTicketFlow() {
        // Example using RestAssured (since you have it in your gradle)
        given()
                .contentType(ContentType.JSON)
                .body("{ \"title\": \"Internet down\", \"description\": \"Help!\" }")
                .when()
                .post("/api/tickets")
                .then()
                .statusCode(201);

        assertTrue(ticketRepository.count() > 0);
    }
}
package de.jonashackt.springredis.controller;

import de.jonashackt.springredis.CoffeeApplication;
import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.web.reactive.function.client.ClientResponse;
import org.springframework.web.reactive.function.client.WebClient;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import reactor.core.publisher.Mono;

import java.io.File;

import static io.restassured.RestAssured.when;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;

@RunWith(SpringRunner.class)
@SpringBootTest(
        classes = CoffeeApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class CoffeeControllerTest {

    @LocalServerPort
    private int port;

    @ClassRule
    public static DockerComposeContainer services =
            new DockerComposeContainer(new File("docker-compose.yml"))
                    .withExposedService("redis", 6379, Wait.forListeningPort());


    @Test
    public void are_coffees_added_to_redis() {

        when()
            .get("http://localhost:" + port + "/coffees")
        .then()
            .statusCode(HttpStatus.SC_OK)
            .assertThat()
                .body(containsString("Black Alert Redis"))
                .body(containsString("Darth Redis"))
                .body(containsString("Jet Black Redis"));
    }

    @Ignore
    @Test
    public void does_app_send_and_receive_reactive_messages() {

        // When we send 3 different coffees
        when().post("http://localhost:" + port + "/message/coffee/fluffyVelour");
        when().post("http://localhost:" + port + "/message/coffee/groomySecret");
        when().post("http://localhost:" + port + "/message/coffee/marryMalone");

        WebClient client = WebClient.create("http://localhost:" + port);
        Mono<ClientResponse> exchange = client.get().uri("/message/coffees").exchange();

        // Given
        // Accessing http://localhost:8080/message/coffees with HTTP GET should trigger receiving messages
        String response = exchange.flatMap(res -> res.bodyToMono(String.class)).block();

        // Then we should receive everything
        assertThat(response, containsString("fluffyVelour"));
    }
}
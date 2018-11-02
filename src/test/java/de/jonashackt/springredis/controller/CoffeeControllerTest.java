package de.jonashackt.springredis.controller;

import de.jonashackt.springredis.SpringredisApplication;
import org.apache.http.HttpStatus;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;

import java.io.File;

import static io.restassured.RestAssured.when;
import static org.hamcrest.Matchers.containsString;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = SpringredisApplication.class)
public class CoffeeControllerTest {

    @ClassRule
    public static DockerComposeContainer services =
            new DockerComposeContainer(new File("../docker-compose.yml"))
                    .withExposedService("redis", 6379, Wait.forListeningPort());
                    //.withExposedService("springredis", 8080, Wait.forHttp("/coffees").forStatusCode(200));


    @Test
    public void does_coffee_work_with_redis() {

        when()
            .get("http://localhost:8080/coffees")
        .then()
            .statusCode(HttpStatus.SC_OK)
            .assertThat()
                .body(containsString("Both Servers called - Alice said 'Alice answering!' & Tom replied 'Tom answering!'."));
    }
}
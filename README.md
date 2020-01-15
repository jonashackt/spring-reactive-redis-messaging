spring-reactive-redis-messaging
======================================================================================
[![Build Status](https://travis-ci.org/jonashackt/spring-reactive-redis-messaging.svg?branch=master)](https://travis-ci.org/jonashackt/spring-reactive-redis-messaging)
[![renovateenabled](https://img.shields.io/badge/renovate-enabled-yellow)](https://renovatebot.com)

Example project showing how to interact with Redis using Spring Boot 

## HowTo

Inspired by https://spring.io/guides/gs/spring-data-reactive-redis/

Go to https://start.spring.io/ and choose `Lombok`, `Reactive Web`, `Reactive Redis`.

The Testcase [CoffeeControllerTest.java](src/test/java/de/jonashackt/springredis/controller/CoffeeControllerTest.java) uses [testcontainers](https://www.testcontainers.org/) framework to leverage [redis](https://redis.io/) as a broker. Just run it, testcontainers will take care of firing up redis with [Docker](https://www.docker.com/) (just be sure to have Docker installed).

If you want to run the [SpringredisApplication.java](src/main/java/de/jonashackt/springredis/SpringredisApplication.java) be sure to fire up redis with:

```
docker-compose up
```

### Implementing Reactive Messaging with Redis

See https://docs.spring.io/spring-data/data-redis/docs/2.1.1.RELEASE/reference/html/#redis:reactive

##### ReactiveRedisConnectionFactory

First we´ll need to configure Spring to [connect reactively to Redis with the ReactiveRedisConnectionFactory](https://docs.spring.io/spring-data/data-redis/docs/2.1.1.RELEASE/reference/html/#redis:reactive:connectors:lettuce) with Lettuce behind the scenes:

```
    @Bean
    public ReactiveRedisConnectionFactory connectionFactory() {
        return new LettuceConnectionFactory("localhost", 6379);
    }
```

##### ReactiveRedisTemplate

Interaction with Redis in reactive use cases is abstracted in Spring Data by ReactiveRedisTemplate. So we need to initialize a Bean:

```
    @Bean
    public ReactiveRedisTemplate<String, String> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory) {
        return new ReactiveRedisTemplate<>(factory, RedisSerializationContext.string());
    }
```

##### Have a look into Redis PubSub

Only if one uses channels and subscribes to it on Redis, these channels are present (see `Understanding channels` in https://redisgreen.net/blog/pubsub-howto/).

To see the current channels in the Redis Docker container, check:

```
docker exec -it redisContainerName sh

redis-cli

pubsub channels
``` 


### REST endpoints

Spring Reactive WebClient: https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#webtestclient-tests

We need to use a curl / Postman / httpie to open up a HTTP connection to retrieve Server Side Events (SSE) from our channel subscriber:

```
curl -v http://localhost:8080/message/coffees
```

Now messages could be retrieved from the Redis Pub/Sub channel `coffees:queue`.

On a second terminal windows we can now publish messages to Redis :

```
http POST http://localhost:8080/message/coffee/fooBarCoffee
```

See https://developer.okta.com/blog/2018/09/24/reactive-apis-with-spring-webflux#the-web-the-final-frontier for more details on how to implement Spring Webflux style REST endpoints.


# Links

### Redis

https://github.com/eugenp/tutorials/blob/master/persistence-modules/spring-data-redis/src/test/java/com/baeldung/spring/data/reactive/redis/template/RedisTemplateListOpsIntegrationTest.java

https://spring.io/guides/gs/messaging-redis/

https://www.baeldung.com/spring-data-redis-pub-sub


### Redis Reactive Messaging

https://spring.io/guides/gs/spring-data-reactive-redis/

https://docs.spring.io/spring-data/data-redis/docs/2.1.1.RELEASE/reference/html/#redis:reactive

https://www.baeldung.com/java-redis-lettuce


### RabbitMQ / AMQP Messaging

https://www.baeldung.com/spring-amqp-reactive


### Reactive WebFlux Stack

https://developer.okta.com/blog/2018/09/24/reactive-apis-with-spring-webflux#the-web-the-final-frontier

https://spring.io/guides/gs/reactive-rest-service/

https://www.baeldung.com/spring-5-webclient

https://docs.spring.io/spring/docs/current/spring-framework-reference/testing.html#webtestclient-tests

https://dzone.com/articles/reactive-programming-with-spring-webflux

https://www.baeldung.com/spring-5-functional-web

https://medium.com/@cheron.antoine/tuto-building-a-reactive-restful-api-with-spring-webflux-java-258fd4dbae41

https://stackoverflow.com/questions/50740795/how-to-wait-for-all-requests-to-complete-with-spring-5-webclient



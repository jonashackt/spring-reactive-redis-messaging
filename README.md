spring-boot-redis
======================================================================================
[![Build Status](https://travis-ci.org/jonashackt/spring-boot-redis.svg?branch=master)](https://travis-ci.org/jonashackt/spring-boot-redis)

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

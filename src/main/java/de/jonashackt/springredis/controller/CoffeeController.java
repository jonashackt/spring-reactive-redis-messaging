package de.jonashackt.springredis.controller;

import de.jonashackt.springredis.domain.Coffee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.ReactiveSubscription;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.ReactiveRedisMessageListenerContainer;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class CoffeeController {

    private static final Logger LOG = LoggerFactory.getLogger(CoffeeController.class);

    private final ReactiveRedisOperations<String, Coffee> coffeeOps;

    @Autowired
    private ReactiveRedisTemplate<String, String> reactiveTemplate;

    @Autowired
    private ReactiveRedisMessageListenerContainer reactiveMsgListenerContainer;

    @Autowired
    private ChannelTopic topic;

    CoffeeController(ReactiveRedisOperations<String, Coffee> coffeeOps) {
        this.coffeeOps = coffeeOps;
    }

    @GetMapping("/coffees")
    public Flux<Coffee> all() {
        LOG.info("Receiving all Coffees from Redis.");
        return coffeeOps.keys("*")
                .flatMap(coffeeOps.opsForValue()::get);
    }

    @PostMapping("/coffee/{variety}")
    public Mono<Long> addCoffee(@PathVariable String variety) {
        LOG.info("New Coffee with variety '" + variety + "' added to Redis.");
        return reactiveTemplate.opsForList().leftPush(UUID.randomUUID().toString(), variety);
    }

    @PostMapping("/message/coffee/{variety}")
    public Mono<Long> sendCoffeeMessage(@PathVariable String variety) {
        LOG.info("New Coffee with variety '" + variety + "' send to Channel '" + topic.getTopic() + "'.");
        return reactiveTemplate.convertAndSend(topic.getTopic(), variety);
    }

    @GetMapping("/message/coffees")
    public Flux<String> receiveCoffeeMessages() {
        LOG.info("Starting to receive Coffee Messages from Channel '" + topic.getTopic() + "'.");
        return reactiveMsgListenerContainer
                .receive(topic)
                .map(ReactiveSubscription.Message::getMessage)
                .map(msg -> {
                    LOG.info("New Message received: '" + msg.toString() + "'.");
                    return msg.toString() + "\n";
                });
    }
}

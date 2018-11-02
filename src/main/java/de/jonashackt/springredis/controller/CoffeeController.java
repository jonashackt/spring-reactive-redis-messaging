package de.jonashackt.springredis.controller;

import de.jonashackt.springredis.configuration.Channel;
import de.jonashackt.springredis.domain.Coffee;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ReactiveRedisTemplate<String, String> template;

    @Autowired
    private ReactiveRedisMessageListenerContainer listenerContainer;

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
        return template.opsForList().leftPush(UUID.randomUUID().toString(), variety);
    }

    @PostMapping("/message/coffee/{variety}")
    public Mono<Long> sendCoffeeMessage(@PathVariable String variety) {
        LOG.info("New Coffee with variety '" + variety + "' send to Channel '" + Channel.COFFEES.topicName() + "'.");
        return template.convertAndSend(Channel.COFFEES.topicName(), variety);
    }

    @GetMapping("/message/coffee")
    public Flux<?> receiveCoffeeMessages() {
        LOG.info("Receiving Coffee Messages from Channel '" + Channel.COFFEES.topicName() + "'.");
        return listenerContainer.receive(ChannelTopic.of(Channel.COFFEES.topicName()));
    }
}

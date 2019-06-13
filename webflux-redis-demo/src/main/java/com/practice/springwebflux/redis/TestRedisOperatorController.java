package com.practice.springwebflux.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.ReactiveRedisOperations;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
public class TestRedisOperatorController {
    @Autowired
    @Qualifier("redisOperations")
    private ReactiveRedisOperations<String, String> tokenOps;


    @RequestMapping("set/{key}/{val}")
    public Mono<Boolean> set(@PathVariable("key") String key, @PathVariable("val") String val) {
        return tokenOps.opsForValue().set(key, val);
    }

    @RequestMapping("get/{key}")
    public Mono<String> get(@PathVariable("key") String key) {
        Mono<String> mono = tokenOps.opsForValue().get(key);
        mono.subscribe(System.out::println);
        return mono;
    }

    @GetMapping("isOk")
    public Object get() {
        return "success";
    }
}

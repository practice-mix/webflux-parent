package com.practice.spring.webflux.copy_body;

import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

/**
 * @author Luo Bao Ding
 * @since 2018/6/8
 */
public class MyHandler {

    public <T extends ServerResponse> Mono<T> persistNotification(ServerRequest request) {
        return Mono.empty();//note: arbitrary addon
    }
}

package com.practice.spring.webflux.demo.service;

import com.practice.spring.webflux.demo.model.ResultViewModel;
import org.springframework.web.reactive.function.server.ServerRequest;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Luo Bao Ding
 * @since 2018/5/25
 */
public interface IDemoService {
    Mono<ResultViewModel> extraResult(ServerRequest request);

    Flux<ResultViewModel> flowAllResult(ServerRequest request);

    Mono<ResultViewModel> putItem(ServerRequest request);
}

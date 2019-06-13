package com.practice.spring.webflux.demo.handler;

import com.practice.spring.webflux.demo.model.ResultViewModel;
import com.practice.spring.webflux.demo.service.IDemoService;
import com.practice.spring.webflux.demo.service.impl.DemoServiceImpl;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * @author Luo Bao Ding
 * @since 2018/5/25
 */
public class DemoHandler {

    private IDemoService demoService =new DemoServiceImpl();

    public Mono<ServerResponse> extraResult(ServerRequest request) {
        Mono<ResultViewModel> resultViewModelMono = demoService.extraResult(request);
        return buildServerResponseMono(resultViewModelMono);
    }

    public Mono<ServerResponse> putItem(ServerRequest request) {
        Mono<ResultViewModel> resultViewModelMono = demoService.putItem(request);
        return buildServerResponseMono(resultViewModelMono);
    }

    public Flux<ServerResponse> flowAllResult(ServerRequest request) {
        Flux<ResultViewModel> resultViewModelFlux = demoService.flowAllResult(request);

        Flux<ServerResponse> serverResponseFlux = resultViewModelFlux.flatMap(resultViewModel ->
                {
                    Mono<ServerResponse> body = ServerResponse.ok()
                            .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(resultViewModel));
                    return body;
                }
        );
        return buildServerResponseFlux(resultViewModelFlux);
    }

    private Flux<ServerResponse> buildServerResponseFlux(Flux<ResultViewModel> resultViewModelFlux) {
        return resultViewModelFlux.flatMap(resultViewModel -> ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON).body(BodyInserters.fromObject(resultViewModel))).switchIfEmpty(
                ServerResponse.notFound().build()
        );
    }


    private Mono<ServerResponse> buildServerResponseMono(Mono<ResultViewModel> resultViewModelMono) {
        return resultViewModelMono.flatMap(resultViewModel -> ServerResponse.ok().contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromObject(resultViewModel))).switchIfEmpty(ServerResponse.notFound().build());
    }

}

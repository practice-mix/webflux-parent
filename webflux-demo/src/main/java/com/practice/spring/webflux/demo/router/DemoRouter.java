package com.practice.spring.webflux.demo.router;

import com.practice.spring.webflux.demo.handler.DemoHandler;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 * @author Luo Bao Ding
 * @since 2018/5/25
 */
public class DemoRouter {

    private DemoHandler demoHandler = new DemoHandler();

    public RouterFunction<ServerResponse> buildRouter() {
        return RouterFunctions.route(RequestPredicates.GET("/get/{id}")
                        .and(RequestPredicates.accept(MediaType.APPLICATION_JSON_UTF8)),
                demoHandler::extraResult)
                .andRoute(RequestPredicates.POST("/put")
                                .and(RequestPredicates.accept(MediaType.APPLICATION_JSON_UTF8)),
                        demoHandler::putItem)
                ;
    }

}

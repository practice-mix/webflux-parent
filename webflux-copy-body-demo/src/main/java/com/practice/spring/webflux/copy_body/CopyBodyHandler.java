package com.practice.spring.webflux.copy_body;

import org.springframework.context.annotation.Bean;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.web.reactive.function.server.RequestPredicates;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.adapter.WebHttpHandlerBuilder;

/**
 * @author Luo Bao Ding
 * @since 2018/6/8
 */
public class CopyBodyHandler {
    @Bean
    public HttpHandler myHandler(MyHandler handler) {
        final RouterFunction<ServerResponse> routerFunction =
                RouterFunctions.route(RequestPredicates.POST("/myResource"), handler::persistNotification);
        return WebHttpHandlerBuilder.webHandler(RouterFunctions.toWebHandler(routerFunction))
                .filter(new TracingFilter())
                .build();
    }
}

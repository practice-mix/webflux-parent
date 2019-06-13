package com.practice.spring.webflux.demo;

import com.practice.spring.webflux.demo.router.DemoRouter;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ReactorHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.ipc.netty.http.server.HttpServer;

import java.io.IOException;

/**
 * @author Luo Bao Ding
 * @since 2018/5/25
 */

public class NettyServer {
    public static void main(String[] args) {
        new NettyServer().nettyServer();
    }

    private DemoRouter demoRouter =new DemoRouter();

    public void nettyServer() {
        RouterFunction<ServerResponse> router = demoRouter.buildRouter();
        HttpHandler httpHandler = RouterFunctions.toHttpHandler(router);
        ReactorHttpHandlerAdapter httpHandlerAdapter = new ReactorHttpHandlerAdapter(httpHandler);

        HttpServer server = HttpServer.create("localhost", 9104);
        server.newHandler(httpHandlerAdapter).block();
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}

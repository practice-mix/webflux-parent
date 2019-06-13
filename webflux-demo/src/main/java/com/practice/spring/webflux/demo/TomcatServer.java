package com.practice.spring.webflux.demo;

import com.practice.spring.webflux.demo.router.DemoRouter;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.http.server.reactive.HttpHandler;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import java.io.IOException;

/**
 * @author Luo Bao Ding
 * @since 2018/5/25
 */
public class TomcatServer {
    public static void main(String[] args) {
        new TomcatServer().tomcatServer();
    }

    private DemoRouter demoRouter=new DemoRouter();

    public void tomcatServer() {
        RouterFunction<ServerResponse> router = demoRouter.buildRouter();
        HttpHandler httpHandler = RouterFunctions.toHttpHandler(router);
        ServletHttpHandlerAdapter httpHandlerAdapter = new ServletHttpHandlerAdapter(httpHandler);

        Tomcat tomcat = new Tomcat();
        tomcat.setHostname("localhost");
        tomcat.setPort(9105);

        Context context = tomcat.addContext("", System.getProperty("java.io.tmpdir"));
        Tomcat.addServlet(context, "httpHandlerServlet", httpHandlerAdapter);
        context.addServletMapping("/", "httpHandlerServlet");
        try {
            tomcat.start();
        } catch (LifecycleException e) {
            e.printStackTrace();
        }

        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}

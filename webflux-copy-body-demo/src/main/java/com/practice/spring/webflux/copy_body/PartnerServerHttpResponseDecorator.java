package com.practice.spring.webflux.copy_body;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;

@Slf4j
class PartnerServerHttpResponseDecorator extends ServerHttpResponseDecorator {


    public PartnerServerHttpResponseDecorator(ServerHttpResponse delegate) {
        super(delegate);
    }
}
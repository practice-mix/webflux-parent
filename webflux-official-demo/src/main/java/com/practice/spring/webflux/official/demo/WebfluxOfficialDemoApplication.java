package com.practice.spring.webflux.official.demo;

import com.practice.spring.webflux.official.demo.client.GreetingWebClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author Luo Bao Ding
 * @since 2018/5/25
 */
@SpringBootApplication
public class WebfluxOfficialDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebfluxOfficialDemoApplication.class, args);

        System.out.println(new GreetingWebClient().getResult());

    }
}

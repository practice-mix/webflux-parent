package com.practice.spring.webflux.post;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.io.FileUrlResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.MediaType;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.FormHttpMessageWriter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ResourceHttpMessageWriter;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.http.codec.multipart.MultipartHttpMessageWriter;
import org.springframework.http.codec.multipart.Part;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * @author Luo Bao Ding
 * @since 2018/6/11
 */
@RestController
//@Controller
public class MultipartController implements InitializingBean {

    private WebClientAutoConfiguration webClientAutoConfiguration;
    private List<HttpMessageWriter<?>> httpMessageWriters;
    private MultipartHttpMessageWriter multipartHttpMessageWriter;


    @PostMapping(value = "/post")
    public @ResponseBody
    Object uploadChatImg(/*ServerWebExchange exchange,*/
//                         @RequestBody Flux<Part> parts,
            @RequestPart("sign") String sign,
            @RequestPart("image") FilePart image
//                         @RequestPart("uid") String uid,
    ) {

        String filename = image.filename();
        System.out.println("filename = " + filename);
//        return exchange.getMultipartData().map(map -> map.get("image"));// note: 不可行
        return "filename = [" + filename + "], sign = [" + sign + "]";
    }

    /**
     * 结论: 转发读过body的请求不可实现或不可取
     */
    @PostMapping("/echo/parts")
    public Object echoParts(ServerWebExchange exchange, @RequestBody Flux<Part> parts,
                                @RequestBody MultiValueMap<String, Part> multiValueMap2
    ) {
        return multiValueMap2.get("image").get(0).content();
/*
//        Caused by: com.fasterxml.jackson.databind.exc.InvalidDefinitionException: No serializer found for class org.springframework.http.codec.multipart.SynchronossPartHttpMessageReader$SynchronossFormFieldPart and no properties discovered to create BeanSerializer (to avoid exception, disable SerializationFeature.FAIL_ON_EMPTY_BEANS) (through reference chain: org.springframework.util.LinkedMultiValueMap["uid"]->java.util.ArrayList[0])
        //        return multiValueMap2;
        */
/*
// exception:  Only one connection receive subscriber allowed
        Mono<MultiValueMap<String, Object>> multiFluxMap2 = parts.collectMultimap(Part::name).map(map -> {
            return new LinkedMultiValueMap<String, Object>(
                    map.entrySet().stream().collect(Collectors.<Map.Entry<String, Collection<Part>>, String, List<Object>>toMap(
                            Map.Entry::getKey, e -> {
                                Collection<Part> partCollection = e.getValue();
                                return partCollection.stream().map(
                                        part -> {
                                            if (part instanceof FilePart) {
                                                FilePart filePart = (FilePart) part;
                                                File file = new File(filePart.filename() + "xxx");
                                                filePart.transferTo(file);
                                                try {
                                                    return new UrlResource(file.toURI());
                                                } catch (Exception e1) {
                                                    e1.printStackTrace();
                                                }
                                                // TODO: 2018/6/14
                                            } else {
                                                assert part instanceof FormFieldPart;
                                                FormFieldPart formFieldPart = (FormFieldPart) part;
                                                return formFieldPart.value();
                                            }
                                            return new Object();

                                        }
                                ).collect(Collectors.toList());
                            }
                    ))
            );
        });
        return multipartHttpMessageWriter.write(multiFluxMap2, ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, Part.class), MediaType.MULTIPART_FORM_DATA,
                exchange.getResponse(), Collections.emptyMap());
*/

//        =========================
/* //不可行    Only one connection receive subscriber allowed
        Mono<MultiValueMap<String, Flux<DataBuffer>>> multiFluxMap = parts.collectMultimap(Part::name).map(map -> {
            return new LinkedMultiValueMap<String, Flux<DataBuffer>>(
                    map.entrySet().stream().collect(Collectors.<Map.Entry<String, Collection<Part>>, String, List<Flux<DataBuffer>>>toMap(
                            Map.Entry::getKey, e -> {
                                Collection<Part> partCollection = e.getValue();
                                return partCollection.stream().map(Part::content).collect(Collectors.toList());
                            }
                    ))
            );
        });
//  exception:      Only one connection receive subscriber allowed
        MultiReadPartHttpMessageWriter writer = new MultiReadPartHttpMessageWriter();
        return writer.write(multiFluxMap, ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, Flux.class),
                MediaType.MULTIPART_FORM_DATA, exchange.getResponse(), Collections.emptyMap());
*/

//        ==========================
        //不可行: "toIterable() is blocking, which is not supported in thread reactor-http-nio-3"
/*
        Mono<MultiValueMap<String, DataBuffer>> multiDataMap = parts.collectMultimap(Part::name).map(map -> {
            return new LinkedMultiValueMap<>(
                    map.entrySet().stream().collect(Collectors.<Map.Entry<String, Collection<Part>>, String, List<DataBuffer>>toMap(
                            Map.Entry::getKey, e -> {
                                Collection<Part> partCollection = e.getValue();
                                Stream<Flux<DataBuffer>> fluxStream = partCollection.stream().map(Part::content);
                                Stream<Iterable<DataBuffer>> iterableStream = fluxStream.map(Flux::toIterable);//exception:  "toIterable() is blocking, which is not supported in thread reactor-http-nio-3"
                                Stream<DataBuffer> dataBufferStream = iterableStream.flatMap(dataBuffers -> StreamSupport.stream(dataBuffers.spliterator(), false));

                                return dataBufferStream.collect(Collectors.toList());
                            }
                    ))
            );
        });
        return multipartHttpMessageWriter.write(multiFluxMap, ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, Part.class), MediaType.MULTIPART_FORM_DATA,
                exchange.getResponse(), Collections.emptyMap());//error: "No suitable writer found for part: uid"
*/
//==============================

/*//
不可行: "toIterable() is blocking, which is not supported in thread reactor-http-nio-3"

       Mono<MultiValueMap<String, Part>> multipartData = exchange.getMultipartData();
        return multipartHttpMessageWriter.write(multiDataMap, ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, Part.class), MediaType.MULTIPART_FORM_DATA,
                exchange.getResponse(), Collections.emptyMap());
        */

//=========================
    }

    private Mono<MultiValueMap<String, Part>> collectMultimap(@RequestBody Flux<Part> parts) {
        return parts.collectMultimap(Part::name).map(this::toMultiValueMap);
    }

    /**
     * success
     */
    @PostMapping("/echo/body")
    public Mono<Void> echoBody(ServerWebExchange exchange) {
        Flux<DataBuffer> body = exchange.getRequest().getBody();
        return exchange.getResponse().writeAndFlushWith(Flux.just(body));
    }

    /**
     * fail:
     * "error": "Internal Server Error",
     * "message": "No suitable writer found for part: uid"
     */
    @PostMapping(value = "/echo/multipart", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<Void> echoMultipart(ServerWebExchange exchange,
                                    @RequestBody Flux<Part> parts,
                                    @RequestBody MultiValueMap<String, Part> multiValueMap) {

      /*  MultipartHttpMessageWriter multipartHttpMessageWriter = new MultipartHttpMessageWriter(Arrays.asList(
                new EncoderHttpMessageWriter<>(CharSequenceEncoder.textPlainOnly()),
                new ResourceHttpMessageWriter()
        ));*/

//        todo : solve  "No suitable writer found for part: uid"
        return multipartHttpMessageWriter.write(Flux.just(multiValueMap), ResolvableType.forClassWithGenerics(MultiValueMap.class, String.class, Part.class), MediaType.MULTIPART_FORM_DATA,
                exchange.getResponse(), Collections.emptyMap());
    }

    private LinkedMultiValueMap<String, Part> toMultiValueMap(Map<String, Collection<Part>> map) {
        return new LinkedMultiValueMap<>(map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, e -> toList(e.getValue()))));
    }

    private List<Part> toList(Collection<Part> collection) {
        return collection instanceof List ? (List<Part>) collection : new ArrayList<>(collection);
    }


    @Autowired
    public void setWebClientAutoConfiguration(WebClientAutoConfiguration webClientAutoConfiguration) {
        this.webClientAutoConfiguration = webClientAutoConfiguration;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
//        WebClient.Builder builder = this.webClientAutoConfiguration.webClientBuilder();

        ExchangeStrategies exchangeStrategies = ExchangeStrategies.withDefaults();

        httpMessageWriters = exchangeStrategies.messageWriters();
        multipartHttpMessageWriter = new MultipartHttpMessageWriter(httpMessageWriters, new FormHttpMessageWriter());

        // TODO: 2018/6/12  obtain message writer
    }
}

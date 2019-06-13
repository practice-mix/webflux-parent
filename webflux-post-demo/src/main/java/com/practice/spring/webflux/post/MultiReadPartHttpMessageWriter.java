/*
 * Copyright 2002-2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.practice.spring.webflux.post;

import org.reactivestreams.Publisher;
import org.springframework.core.ResolvableType;
import org.springframework.core.codec.CharSequenceEncoder;
import org.springframework.core.codec.CodecException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DefaultDataBufferFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ReactiveHttpOutputMessage;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.http.codec.EncoderHttpMessageWriter;
import org.springframework.http.codec.FormHttpMessageWriter;
import org.springframework.http.codec.HttpMessageWriter;
import org.springframework.http.codec.ResourceHttpMessageWriter;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 服务转发 multipart server http Request
 * implements HttpMessageWriter<MultiValueMap<String, Flux<DataBuffer>>>
 *
 * @see FormHttpMessageWriter
 * @since 5.0
 */
public class MultiReadPartHttpMessageWriter implements HttpMessageWriter<MultiValueMap<String, ?>> {

    public static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;


    private Charset charset = DEFAULT_CHARSET;

    private final List<MediaType> supportedMediaTypes;

    private final DataBufferFactory bufferFactory = new DefaultDataBufferFactory();


    public MultiReadPartHttpMessageWriter() {
        this.supportedMediaTypes = initMediaTypes();
    }

    private static List<MediaType> initMediaTypes() {
        List<MediaType> result = new ArrayList<>();
        result.add(MediaType.MULTIPART_FORM_DATA);
        return Collections.unmodifiableList(result);
    }


    /**
     * Set the character set to use for part headers such as
     * "Content-Disposition" (and its filename parameter).
     * <p>By default this is set to "UTF-8".
     */
    public void setCharset(Charset charset) {
        Assert.notNull(charset, "Charset must not be null");
        this.charset = charset;
    }

    /**
     * Return the configured charset for part headers.
     */
    public Charset getCharset() {
        return this.charset;
    }


    @Override
    public List<MediaType> getWritableMediaTypes() {
        return this.supportedMediaTypes;
    }

    @Override
    public boolean canWrite(ResolvableType elementType, @Nullable MediaType mediaType) {
        Class<?> rawClass = elementType.getRawClass();
        return rawClass != null && MultiValueMap.class.isAssignableFrom(rawClass) &&
                (mediaType == null ||
                        this.supportedMediaTypes.stream().anyMatch(m -> m.isCompatibleWith(mediaType)));
    }

    @Override
    public Mono<Void> write(Publisher<? extends MultiValueMap<String, ?>> inputStream,
                            ResolvableType elementType, @Nullable MediaType mediaType, ReactiveHttpOutputMessage outputMessage,
                            Map<String, Object> hints) {

        return Mono.from(inputStream).flatMap(map -> writeMultipart(map, outputMessage));
    }


    private Mono<Void> writeMultipart(MultiValueMap<String, ?> map, ReactiveHttpOutputMessage outputMessage) {
        byte[] boundary = generateMultipartBoundary();

        Map<String, String> params = new HashMap<>(2);
        params.put("boundary", new String(boundary, StandardCharsets.US_ASCII));
        params.put("charset", getCharset().name());

        outputMessage.getHeaders().setContentType(new MediaType(MediaType.MULTIPART_FORM_DATA, params));

        Flux<DataBuffer> body = Flux.fromIterable(map.entrySet())
                .concatMap(entry -> encodePartValues(boundary, entry.getKey(), entry.getValue()))
                .concatWith(Mono.just(generateLastLine(boundary)));

        return outputMessage.writeWith(body);
    }

    /**
     * Generate a multipart boundary.
     * <p>By default delegates to {@link MimeTypeUtils#generateMultipartBoundary()}.
     */
    protected byte[] generateMultipartBoundary() {
        return MimeTypeUtils.generateMultipartBoundary();
    }

    /**
     * @param values List<Flux<DataBuffer>>
     */
    private Flux<DataBuffer> encodePartValues(byte[] boundary, String name, List<?> values) {
        return Flux.concat(values.stream().map(v ->
                encodePart(boundary, name, v)).collect(Collectors.toList()));
    }

    /**
     * @param <T> List<Flux<DataBuffer>>
     */
    @SuppressWarnings("unchecked")
    private <T> Flux<DataBuffer> encodePart(byte[] boundary, String name, T value) {
        return Flux.concat(Mono.just(generateBoundaryLine(boundary)), (Flux<DataBuffer>) value, Mono.just(generateNewLine()));
    }


    private DataBuffer generateBoundaryLine(byte[] boundary) {
        DataBuffer buffer = this.bufferFactory.allocateBuffer(boundary.length + 4);
        buffer.write((byte) '-');
        buffer.write((byte) '-');
        buffer.write(boundary);
        buffer.write((byte) '\r');
        buffer.write((byte) '\n');
        return buffer;
    }

    private DataBuffer generateNewLine() {
        DataBuffer buffer = this.bufferFactory.allocateBuffer(2);
        buffer.write((byte) '\r');
        buffer.write((byte) '\n');
        return buffer;
    }

    private DataBuffer generateLastLine(byte[] boundary) {
        DataBuffer buffer = this.bufferFactory.allocateBuffer(boundary.length + 6);
        buffer.write((byte) '-');
        buffer.write((byte) '-');
        buffer.write(boundary);
        buffer.write((byte) '-');
        buffer.write((byte) '-');
        buffer.write((byte) '\r');
        buffer.write((byte) '\n');
        return buffer;
    }


    private static class MultipartHttpOutputMessage implements ReactiveHttpOutputMessage {

        private final DataBufferFactory bufferFactory;

        private final Charset charset;

        private final HttpHeaders headers = new HttpHeaders();

        private final AtomicBoolean committed = new AtomicBoolean();

        @Nullable
        private Flux<DataBuffer> body;

        public MultipartHttpOutputMessage(DataBufferFactory bufferFactory, Charset charset) {
            this.bufferFactory = bufferFactory;
            this.charset = charset;
        }

        @Override
        public HttpHeaders getHeaders() {
            return (this.body != null ? HttpHeaders.readOnlyHttpHeaders(this.headers) : this.headers);
        }

        @Override
        public DataBufferFactory bufferFactory() {
            return this.bufferFactory;
        }

        @Override
        public void beforeCommit(Supplier<? extends Mono<Void>> action) {
            this.committed.set(true);
        }

        @Override
        public boolean isCommitted() {
            return this.committed.get();
        }

        @Override
        public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
            if (this.body != null) {
                return Mono.error(new IllegalStateException("Multiple calls to writeWith() not supported"));
            }
            this.body = Flux.just(generateHeaders()).concatWith(body);

            // We don't actually want to write (just save the body Flux)
            return Mono.empty();
        }

        private DataBuffer generateHeaders() {
            DataBuffer buffer = this.bufferFactory.allocateBuffer();
            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                byte[] headerName = entry.getKey().getBytes(this.charset);
                for (String headerValueString : entry.getValue()) {
                    byte[] headerValue = headerValueString.getBytes(this.charset);
                    buffer.write(headerName);
                    buffer.write((byte) ':');
                    buffer.write((byte) ' ');
                    buffer.write(headerValue);
                    buffer.write((byte) '\r');
                    buffer.write((byte) '\n');
                }
            }
            buffer.write((byte) '\r');
            buffer.write((byte) '\n');
            return buffer;
        }

        @Override
        public Mono<Void> writeAndFlushWith(Publisher<? extends Publisher<? extends DataBuffer>> body) {
            return Mono.error(new UnsupportedOperationException());
        }

        public Flux<DataBuffer> getBody() {
            return (this.body != null ? this.body :
                    Flux.error(new IllegalStateException("Body has not been written yet")));
        }

        @Override
        public Mono<Void> setComplete() {
            return Mono.error(new UnsupportedOperationException());
        }
    }

}

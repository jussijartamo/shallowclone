package org.shallowclone.proxy;

import io.undertow.Undertow;
import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.server.handlers.ResponseCodeHandler;
import io.undertow.server.handlers.proxy.LoadBalancingProxyClient;
import io.undertow.server.handlers.proxy.ProxyHandler;
import io.undertow.util.HeaderMap;
import io.undertow.util.HeaderValues;
import io.undertow.util.Headers;
import io.undertow.util.HttpString;
import org.asynchttpclient.AsyncHttpClient;
import org.asynchttpclient.BoundRequestBuilder;
import org.asynchttpclient.DefaultAsyncHttpClient;
import org.asynchttpclient.Response;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static java.util.Arrays.*;
import static java.util.stream.Stream.*;

public class RecordingHttpProxy {
    public static final Integer DEFAULT_PORT = 9000;
    public static final String JVM_PORT_FLAG = "port";
    public static Optional<Integer> findPort(Stream<String> args) {
        return args.flatMap(s -> {
            try {
                int port = Integer.parseInt(s);
                return Stream.of(port);
            } catch (Throwable t) {
                return Stream.empty();
            }
        }).findAny();
    }

    public static void main(String[] args) throws Exception {
        final int port = findPort(concat(of(System.getProperty(JVM_PORT_FLAG)), asList(args).stream())).orElse(DEFAULT_PORT);

        /*
        LoadBalancingProxyClient loadBalancer = new LoadBalancingProxyClient()
                .addHost(new URI("https://github.com"))
                .setConnectionsPerThread(20);

        Undertow reverseProxy = Undertow.builder()
                .addHttpListener(9000, "localhost")
                .setIoThreads(4)
                .setHandler(new ProxyHandler(loadBalancer, 30000, ResponseCodeHandler.HANDLE_404))
                .build();
        reverseProxy.start();
        */
        final AsyncHttpClient asyncHttpClient = new DefaultAsyncHttpClient();
        Undertow server = Undertow.builder()
                .addHttpListener(9000, "localhost")
                .setHandler(new HttpHandler() {
                    @Override
                    public void handleRequest(final HttpServerExchange exchange) throws Exception {

                        HeaderMap requestHeaders = exchange.getRequestHeaders();
                        /*exchange.getResponseHeaders().put(Headers.CONTENT_TYPE, "text/plain");
                        exchange.getResponseSender().send("Hello World");*/

                        if(!"GET".equals(exchange.getRequestMethod().toString())) {
                            throw new RuntimeException("Exchange method is "+ exchange.getRequestMethod());
                        }



                        BoundRequestBuilder reqBuilder = asyncHttpClient.prepareGet("https://github.com" + exchange.getRequestPath());
                        exchange.getQueryParameters().forEach((q,p) -> {
                            if(p.size() != 1) throw new RuntimeException("More than one param!");
                            reqBuilder.addQueryParam(q, p.getFirst());
                        });

                        for(HeaderValues v : exchange.getRequestHeaders()) {
                            String value = v.getLast().replaceAll("http", "https").replaceAll("localhost", "github.com");
                            reqBuilder.addHeader(v.getHeaderName().toString(), value);
                        }
                        Response response = reqBuilder.execute().toCompletableFuture().get();
                        exchange.setResponseCode(response.getStatusCode());
                        for(Map.Entry<String, String> entry :response.getHeaders()) {
                            exchange.getResponseHeaders().add(new HttpString(entry.getKey()),entry.getValue());
                        }

                        exchange.getResponseSender().send(response.getResponseBodyAsByteBuffer());
                    }
                }).build();
        server.start();

    }
}

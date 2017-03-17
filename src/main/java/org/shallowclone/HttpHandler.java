package org.shallowclone;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public interface HttpHandler {

    /**
     * @return InputStream containing response body
     */
    CompletableFuture<InputStream> handleRequest(String url, Map<String,String> headers, Map<String,String> queryParams);
}

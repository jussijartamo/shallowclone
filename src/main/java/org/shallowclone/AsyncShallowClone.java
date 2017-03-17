package org.shallowclone;

import java.io.InputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public interface AsyncShallowClone {

    CompletableFuture<Iterable<InputStream>> shallowClone(String url);

}

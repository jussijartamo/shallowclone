package org.shallowclone;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.Optional.*;
import static java.util.concurrent.CompletableFuture.completedFuture;

public class ShallowCloneBuilder {
    private Optional<ThreadFactory> threadFactoryOpt = empty();

    public ShallowCloneBuilder() {
    }

    public static ShallowClone createSynchronousShallowClone() {
        return null;
    }

    protected static boolean defaultCreateDaemonThreads() {
        return false;
    }

    protected static ThreadFactory createDefaultThreadFactory(boolean createDaemonThreads) {
        return new ShallowCloneDefaultThreadFactory(createDaemonThreads);
    }

    private static class SyncHttpHandler implements HttpHandler {

        public CompletableFuture<InputStream> handleRequest(String url, Map<String, String> headers, Map<String, String> queryParams) {
            try {
                final Function<String, String> encode = (in) -> {
                    try {
                        final String charset = "UTF-8";
                        return URLEncoder.encode(in, charset);
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                };
                URL u;
                if(!queryParams.isEmpty()) {
                    u = new URL(String.format("%s?%s", url,
                            queryParams.entrySet().stream().map((entry) ->
                            String.format("%s=%s", entry.getKey(), encode.apply(entry.getValue())))
                                    .collect(Collectors.joining("&"))));
                } else {
                    u = new URL(url);
                }
                URLConnection c = u.openConnection();
                headers.forEach((key,value) -> c.setRequestProperty(key, value));
                return completedFuture(c.getInputStream());
            }catch (Exception e) {
                CompletableFuture<InputStream> dummyFuture = new CompletableFuture<>();
                dummyFuture.completeExceptionally(e);
                return dummyFuture;
            }
        }
    }

    private static class ShallowCloneDefaultThreadFactory implements ThreadFactory {
        private static final AtomicInteger poolNumber = new AtomicInteger(1);
        private final ThreadGroup group;
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        private final boolean createDaemonThreads;

        ShallowCloneDefaultThreadFactory(boolean createDaemonThreads) {
            this.createDaemonThreads = createDaemonThreads;
            SecurityManager s = System.getSecurityManager();
            group = (s != null) ? s.getThreadGroup() :
                    Thread.currentThread().getThreadGroup();
            namePrefix = "shallow-clone-pool-" +
                    poolNumber.getAndIncrement() +
                    "-thread-";
        }

        public Thread newThread(Runnable r) {
            Thread t = new Thread(group, r,
                    namePrefix + threadNumber.getAndIncrement(),
                    0);
            if (t.isDaemon())
                t.setDaemon(createDaemonThreads);
            if (t.getPriority() != Thread.NORM_PRIORITY)
                t.setPriority(Thread.NORM_PRIORITY);
            return t;
        }
    }
}

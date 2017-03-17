package org.shallowclone;

import java.io.InputStream;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadFactory;

import static org.shallowclone.ShallowCloneBuilder.*;

public class ShallowClone implements AsyncShallowClone, SyncShallowClone {

    public ShallowClone() {
        this(createDefaultThreadFactory(defaultCreateDaemonThreads()));
    }

    private ShallowClone(ThreadFactory threadFactory) {

    }

    public CompletableFuture<Iterable<InputStream>> shallowClone(String url) {

        return null;
    }
}

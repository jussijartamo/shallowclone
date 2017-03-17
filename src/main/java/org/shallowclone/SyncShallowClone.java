package org.shallowclone;

import java.io.InputStream;

public interface SyncShallowClone extends AsyncShallowClone {

    default Iterable<InputStream> shallowCloneSync(String url) {
        try {
            return shallowClone(url).get();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

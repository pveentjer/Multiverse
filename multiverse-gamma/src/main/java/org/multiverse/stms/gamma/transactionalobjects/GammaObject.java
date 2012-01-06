package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.Lock;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;

public interface GammaObject extends GammaConstants {

    long getVersion();

    GammaStm getStm();

    Lock getLock();

    int identityHashCode();
}

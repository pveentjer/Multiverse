package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.TxnLock;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaStm;

public interface GammaObject extends GammaConstants {

    long getVersion();

    GammaStm getStm();

    TxnLock getLock();

    int identityHashCode();
}

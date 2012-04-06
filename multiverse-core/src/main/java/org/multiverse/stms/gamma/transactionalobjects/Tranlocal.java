package org.multiverse.stms.gamma.transactionalobjects;

import org.multiverse.api.functions.Function;
import org.multiverse.stms.gamma.GammaConstants;
import org.multiverse.stms.gamma.GammaObjectPool;

@SuppressWarnings({"ClassWithTooManyFields"})
public final class Tranlocal<E> implements GammaConstants {

    public E ref_value;
    public long version;
    public int lockMode;
    public BaseGammaTxnRef owner;
    public int mode;
    public boolean hasDepartObligation;
    public boolean isDirty;
    public Tranlocal next;
    public Tranlocal previous;
    public CallableNode headCallable;
    public boolean writeSkewCheck;

    public long long_oldValue;
    public E ref_oldValue;
    public long long_value;


    public boolean isDirty() {
        return isDirty;
    }

    public void setDirty(boolean dirty) {
        isDirty = dirty;
    }

    public int getLockMode() {
        return lockMode;
    }

    public void setLockMode(int lockMode) {
        this.lockMode = lockMode;
    }

    public boolean hasDepartObligation() {
        return hasDepartObligation;
    }

    public void setDepartObligation(boolean b) {
        this.hasDepartObligation = b;
    }

    public boolean isCommuting() {
        return mode == TRANLOCAL_COMMUTING;
    }

    public boolean isConstructing() {
        return mode == TRANLOCAL_CONSTRUCTING;
    }

    public boolean isRead() {
        return mode == TRANLOCAL_READ;
    }

    public boolean isWrite() {
        return mode == TRANLOCAL_WRITE;
    }

    public void addCommutingFunction(GammaObjectPool pool, Function function) {
        final CallableNode newHead = pool.takeCallableNode();
        newHead.function = function;
        newHead.next = headCallable;
        headCallable = newHead;
    }

    public int getMode() {
        return mode;
    }

    public boolean isConflictCheckNeeded() {
        return writeSkewCheck;
    }
}

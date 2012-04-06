package org.multiverse.stms.gamma.transactionalobjects.refs;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.stms.gamma.*;
import org.multiverse.stms.gamma.transactionalobjects.*;
import org.multiverse.stms.gamma.transactions.GammaTxn;

import static org.junit.Assert.*;
import static org.multiverse.stms.gamma.GammaStmUtils.doubleAsLong;
import static org.multiverse.stms.gamma.GammaTestUtils.assertRefHasNoLocks;
import static org.multiverse.stms.gamma.GammaTestUtils.assertVersionAndValue;

public class CommitTest implements GammaConstants {

    private GammaStm stm;
    private GammaObjectPool pool;

    @Before
    public void setUp() {
        stm = new GammaStm();
        pool = new GammaObjectPool();
    }

    @Test
    public void intRef_whenDirty() {
        int initialValue = 10;
        GammaTxnInteger ref = new GammaTxnInteger(stm, initialValue);
        long initialVersion = ref.version;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        int newValue = 20;

        tranlocal.long_value = newValue;
        tranlocal.setDirty(true);
        Listeners listeners = ref.commit(tranlocal, pool);

        assertNull(listeners);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void intRef_whenNotDirty() {
        int initialValue = 10;
        GammaTxnInteger ref = new GammaTxnInteger(stm, initialValue);
        long initialVersion = ref.version;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        tranlocal.setDirty(false);
        Listeners listeners = ref.commit(tranlocal, pool);

        assertNull(listeners);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void intRef_whenDirtyAndListener() {
        int initialValue = 10;
        GammaTxnInteger ref = new GammaTxnInteger(stm, initialValue);
        long initialVersion = ref.version;

        Listeners listeners = new Listeners();
        ref.listeners = listeners;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        int newValue = 20;

        tranlocal.long_value = newValue;
        tranlocal.setDirty(true);
        Listeners result = ref.commit(tranlocal, pool);

        assertSame(result, listeners);
        assertNull(ref.listeners);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void intRef_whenNotDirtyAndListener() {
        int initialValue = 10;
        GammaTxnInteger ref = new GammaTxnInteger(stm, initialValue);
        long initialVersion = ref.version;

        Listeners listeners = new Listeners();
        ref.listeners = listeners;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        tranlocal.setDirty(false);
        Listeners result = ref.commit(tranlocal, pool);

        assertNull(result);
        assertSame(listeners, ref.listeners);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void booleanRef_whenDirty() {
        boolean initialValue = true;
        GammaTxnBoolean ref = new GammaTxnBoolean(stm, initialValue);
        long initialVersion = ref.version;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        boolean newValue = false;

        tranlocal.long_value = GammaStmUtils.booleanAsLong(newValue);
        tranlocal.setDirty(true);
        Listeners listeners = ref.commit(tranlocal, pool);

        assertNull(listeners);
        assertVersionAndValue(ref, initialVersion + 1, false);
        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }


    @Test
    public void booleanRef_whenNotDirty() {
        boolean initialValue = false;
        GammaTxnBoolean ref = new GammaTxnBoolean(stm, initialValue);
        long initialVersion = ref.version;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        tranlocal.setDirty(false);
        Listeners listeners = ref.commit(tranlocal, pool);

        assertNull(listeners);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void booleanRef_whenDirtyAndListener() {
        boolean initialValue = true;
        GammaTxnBoolean ref = new GammaTxnBoolean(stm, initialValue);
        long initialVersion = ref.version;

        Listeners listeners = new Listeners();
        ref.listeners = listeners;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        boolean newValue = false;

        tranlocal.long_value = GammaStmUtils.booleanAsLong(newValue);
        tranlocal.setDirty(true);
        Listeners result = ref.commit(tranlocal, pool);

        assertSame(result, listeners);
        assertNull(ref.listeners);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void booleanRef_whenNotDirtyAndListener() {
        boolean initialValue = true;
        GammaTxnBoolean ref = new GammaTxnBoolean(stm, initialValue);
        long initialVersion = ref.version;

        Listeners listeners = new Listeners();
        ref.listeners = listeners;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        tranlocal.setDirty(false);
        Listeners result = ref.commit(tranlocal, pool);

        assertNull(result);
        assertSame(listeners, ref.listeners);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void TxnDouble_whenDirty() {
        double initialValue = 10;
        GammaTxnDouble ref = new GammaTxnDouble(stm, initialValue);
        long initialVersion = ref.version;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        double newValue = 20;

        tranlocal.long_value = doubleAsLong(newValue);
        tranlocal.setDirty(true);
        Listeners listeners = ref.commit(tranlocal, pool);

        assertNull(listeners);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void TxnDouble_whenNotDirty() {
        double initialValue = 10;
        GammaTxnDouble ref = new GammaTxnDouble(stm, initialValue);
        long initialVersion = ref.version;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        tranlocal.setDirty(false);
        Listeners listeners = ref.commit(tranlocal, pool);

        assertNull(listeners);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void TxnDouble_whenDirtyAndListener() {
        double initialValue = 10;
        GammaTxnDouble ref = new GammaTxnDouble(stm, initialValue);
        long initialVersion = ref.version;

        Listeners listeners = new Listeners();
        ref.listeners = listeners;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        double newValue = 20;

        tranlocal.long_value = doubleAsLong(newValue);
        tranlocal.setDirty(true);
        Listeners result = ref.commit(tranlocal, pool);

        assertSame(result, listeners);
        assertNull(ref.listeners);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void TxnDouble_whenNotDirtyAndListener() {
        double initialValue = 10;
        GammaTxnDouble ref = new GammaTxnDouble(stm, initialValue);
        long initialVersion = ref.version;

        Listeners listeners = new Listeners();
        ref.listeners = listeners;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        tranlocal.setDirty(false);
        Listeners result = ref.commit(tranlocal, pool);

        assertNull(result);
        assertSame(listeners, ref.listeners);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void longRef_whenDirty() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.version;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        long newValue = 20;

        tranlocal.long_value = newValue;
        tranlocal.setDirty(true);
        Listeners listeners = ref.commit(tranlocal, pool);

        assertNull(listeners);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void longRef_whenNotDirty() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.version;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        tranlocal.setDirty(false);
        Listeners listeners = ref.commit(tranlocal, pool);

        assertNull(listeners);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void longRef_whenDirtyAndListener() {
        long initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.version;

        Listeners listeners = new Listeners();
        ref.listeners = listeners;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        long newValue = 20;

        tranlocal.long_value = newValue;
        tranlocal.setDirty(true);
        Listeners result = ref.commit(tranlocal, pool);

        assertSame(result, listeners);
        assertNull(ref.listeners);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }

    @Test
    public void longRef_whenNotDirtyAndListener() {
        int initialValue = 10;
        GammaTxnLong ref = new GammaTxnLong(stm, initialValue);
        long initialVersion = ref.version;

        Listeners listeners = new Listeners();
        ref.listeners = listeners;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        tranlocal.setDirty(false);
        Listeners result = ref.commit(tranlocal, pool);

        assertNull(result);
        assertSame(listeners, ref.listeners);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
    }


    @Test
    public void ref_whenDirty() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.version;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        String newValue = "bar";

        tranlocal.ref_value = newValue;
        tranlocal.setDirty(true);
        Listeners listeners = ref.commit(tranlocal, pool);

        assertNull(listeners);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
    }

    @Test
    public void ref_whenNotDirty() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.version;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        tranlocal.setDirty(false);
        Listeners listeners = ref.commit(tranlocal, pool);

        assertNull(listeners);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
    }

    @Test
    public void ref_whenDirtyAndListener() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.version;

        Listeners listeners = new Listeners();
        ref.listeners = listeners;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        String newValue = "bar";

        tranlocal.ref_value = newValue;
        tranlocal.setDirty(true);
        Listeners result = ref.commit(tranlocal, pool);

        assertSame(result, listeners);
        assertNull(ref.listeners);
        assertVersionAndValue(ref, initialVersion + 1, newValue);
        assertRefHasNoLocks(ref);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
    }

    @Test
    public void ref_whenNotDirtyAndListener() {
        String initialValue = "foo";
        GammaTxnRef<String> ref = new GammaTxnRef<String>(stm, initialValue);
        long initialVersion = ref.version;

        Listeners listeners = new Listeners();
        ref.listeners = listeners;

        Tranlocal tranlocal = new Tranlocal();
        GammaTxn tx = stm.newDefaultTxn();
        ref.load(tx,tranlocal, LOCKMODE_EXCLUSIVE, 1, false);

        tranlocal.setDirty(false);
        Listeners result = ref.commit(tranlocal, pool);

        assertNull(result);
        assertSame(listeners, ref.listeners);
        assertVersionAndValue(ref, initialVersion, initialValue);
        assertNull(tranlocal.owner);
        assertEquals(LOCKMODE_NONE, tranlocal.getLockMode());
        assertNull(tranlocal.ref_value);
        assertNull(tranlocal.ref_oldValue);
    }

}

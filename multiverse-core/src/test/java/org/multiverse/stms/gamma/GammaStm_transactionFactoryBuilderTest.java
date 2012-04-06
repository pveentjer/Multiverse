package org.multiverse.stms.gamma;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.*;
import org.multiverse.api.exceptions.IllegalTxnFactoryException;
import org.multiverse.api.lifecycle.TxnListener;
import org.multiverse.stms.gamma.transactions.GammaTxn;
import org.multiverse.stms.gamma.transactions.GammaTxnConfig;
import org.multiverse.stms.gamma.transactions.GammaTxnFactory;
import org.multiverse.stms.gamma.transactions.GammaTxnFactoryBuilder;
import org.multiverse.stms.gamma.transactions.fat.FatMonoGammaTxn;

import java.util.List;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class GammaStm_transactionFactoryBuilderTest implements GammaConstants{
    private GammaStm stm;

    @Before
    public void setUp() {
        stm = new GammaStm();
    }

    @Test
    public void whenDefaultTransactionFactory() {
        GammaTxnConfig config = new GammaTxnConfig(stm);
        config.init();

        assertEquals(IsolationLevel.Snapshot, config.isolationLevel);
        assertFalse(config.isInterruptible());
        assertFalse(config.isReadonly());
        assertEquals(LockMode.None, config.readLockMode);
        assertEquals(LockMode.None, config.writeLockMode);
        assertTrue(config.dirtyCheck);
        assertSame(stm, config.getStm());
        assertSame(stm.getGlobalConflictCounter(), config.getGlobalConflictCounter());
        assertTrue(config.trackReads);
        assertTrue(config.blockingAllowed);
        assertEquals(1000, config.maxRetries);
        assertTrue(config.isSpeculative());
        assertTrue(config.isAnonymous);
        assertSame(DefaultBackoffPolicy.MAX_100_MS, config.getBackoffPolicy());
        assertEquals(Long.MAX_VALUE, config.getTimeoutNs());
        assertSame(TraceLevel.None, config.getTraceLevel());
        assertTrue(config.writeSkewAllowed);
        assertEquals(PropagationLevel.Requires, config.getPropagationLevel());
        assertTrue(config.getPermanentListeners().isEmpty());
    }

    @Test(expected = NullPointerException.class)
    public void whenNullPermanentListener_thenNullPointerException() {
        stm.newTxnFactoryBuilder().addPermanentListener(null);
    }

    @Test
    public void whenPermanentListenerAdded() {
        GammaTxnFactoryBuilder oldBuilder = stm.newTxnFactoryBuilder();
        TxnListener listener = mock(TxnListener.class);
        GammaTxnFactoryBuilder newBuilder = oldBuilder.addPermanentListener(listener);

        assertEquals(asList(listener), newBuilder.getConfiguration().getPermanentListeners());
        assertTrue(oldBuilder.getConfiguration().getPermanentListeners().isEmpty());
    }

    @Test
    public void whenPermanentListenerAdded_thenNoCheckForDuplicates() {
        GammaTxnFactoryBuilder oldBuilder = stm.newTxnFactoryBuilder();
        TxnListener listener = mock(TxnListener.class);
        GammaTxnFactoryBuilder newBuilder = oldBuilder.addPermanentListener(listener)
                .addPermanentListener(listener);

        assertEquals(asList(listener, listener), newBuilder.getConfiguration().getPermanentListeners());
    }

    @Test
    public void whenNoPermanentListenersAdded_thenEmptyList() {
        GammaTxnFactoryBuilder builder = stm.newTxnFactoryBuilder();
        assertTrue(builder.getConfiguration().getPermanentListeners().isEmpty());
    }

    @Test
    public void whenMultipleListenersAdded_thenTheyAreAddedInOrder() {
        TxnListener listener1 = mock(TxnListener.class);
        TxnListener listener2 = mock(TxnListener.class);
        GammaTxnFactoryBuilder builder = stm.newTxnFactoryBuilder()
                .addPermanentListener(listener1)
                .addPermanentListener(listener2);

        List<TxnListener> listeners = builder.getConfiguration().getPermanentListeners();
        assertEquals(asList(listener1, listener2), listeners);
    }

    @Test
    public void whenGetPermanentListenersCalled_immutableListReturned() {
        GammaTxnFactoryBuilder builder = stm.newTxnFactoryBuilder()
                .addPermanentListener(mock(TxnListener.class))
                .addPermanentListener(mock(TxnListener.class));

        List<TxnListener> listeners = builder.getConfiguration().getPermanentListeners();

        try {
            listeners.clear();
            fail();
        } catch (UnsupportedOperationException e) {
        }
    }

    @Test
    public void whenReadLockModeOverridesWriteLockMode() {
        whenReadLockModeOverridesWriteLockMode(LockMode.None, LockMode.None);
        whenReadLockModeOverridesWriteLockMode(LockMode.None, LockMode.Read);
        whenReadLockModeOverridesWriteLockMode(LockMode.None, LockMode.Write);
        whenReadLockModeOverridesWriteLockMode(LockMode.None, LockMode.Exclusive);

        whenReadLockModeOverridesWriteLockMode(LockMode.Read, LockMode.None);
        whenReadLockModeOverridesWriteLockMode(LockMode.Read, LockMode.Read);
        whenReadLockModeOverridesWriteLockMode(LockMode.Read, LockMode.Write);
        whenReadLockModeOverridesWriteLockMode(LockMode.Read, LockMode.Exclusive);

        whenReadLockModeOverridesWriteLockMode(LockMode.Write, LockMode.None);
        whenReadLockModeOverridesWriteLockMode(LockMode.Write, LockMode.Read);
        whenReadLockModeOverridesWriteLockMode(LockMode.Write, LockMode.Write);
        whenReadLockModeOverridesWriteLockMode(LockMode.Write, LockMode.Exclusive);

        whenReadLockModeOverridesWriteLockMode(LockMode.Exclusive, LockMode.None);
        whenReadLockModeOverridesWriteLockMode(LockMode.Exclusive, LockMode.Read);
        whenReadLockModeOverridesWriteLockMode(LockMode.Exclusive, LockMode.Write);
        whenReadLockModeOverridesWriteLockMode(LockMode.Exclusive, LockMode.Exclusive);
    }

    public void whenReadLockModeOverridesWriteLockMode(LockMode readLockMode, LockMode writeLockMode) {
        GammaTxnFactory txFactory = stm.newTxnFactoryBuilder()
                .setWriteLockMode(writeLockMode)
                .setReadLockMode(readLockMode)
                .newTransactionFactory();

        assertEquals(readLockMode, txFactory.getConfiguration().readLockMode);
        assertEquals(readLockMode.asInt(), txFactory.getConfiguration().readLockModeAsInt);

        if (readLockMode.asInt() > writeLockMode.asInt()) {
            assertEquals(readLockMode, txFactory.getConfiguration().getWriteLockMode());
            assertEquals(readLockMode.asInt(), txFactory.getConfiguration().writeLockModeAsInt);
        } else {
            assertEquals(writeLockMode, txFactory.getConfiguration().getWriteLockMode());
            assertEquals(writeLockMode.asInt(), txFactory.getConfiguration().writeLockModeAsInt);
        }
    }

    @Test
    public void whenWriteLockModeOverridesReadLockMode() {
        whenWriteLockModeOverridesReadLockMode(LockMode.None, LockMode.None, true);
        whenWriteLockModeOverridesReadLockMode(LockMode.None, LockMode.Read, true);
        whenWriteLockModeOverridesReadLockMode(LockMode.None, LockMode.Write, true);
        whenWriteLockModeOverridesReadLockMode(LockMode.None, LockMode.Exclusive, true);

        whenWriteLockModeOverridesReadLockMode(LockMode.Read, LockMode.None, false);
        whenWriteLockModeOverridesReadLockMode(LockMode.Read, LockMode.Read, true);
        whenWriteLockModeOverridesReadLockMode(LockMode.Read, LockMode.Write, true);
        whenWriteLockModeOverridesReadLockMode(LockMode.Read, LockMode.Exclusive, true);

        whenWriteLockModeOverridesReadLockMode(LockMode.Write, LockMode.None, false);
        whenWriteLockModeOverridesReadLockMode(LockMode.Write, LockMode.Read, false);
        whenWriteLockModeOverridesReadLockMode(LockMode.Write, LockMode.Write, true);
        whenWriteLockModeOverridesReadLockMode(LockMode.Write, LockMode.Exclusive, true);

        whenWriteLockModeOverridesReadLockMode(LockMode.Exclusive, LockMode.None, false);
        whenWriteLockModeOverridesReadLockMode(LockMode.Exclusive, LockMode.Read, false);
        whenWriteLockModeOverridesReadLockMode(LockMode.Exclusive, LockMode.Write, false);
        whenWriteLockModeOverridesReadLockMode(LockMode.Exclusive, LockMode.Exclusive, true);
    }

    public void whenWriteLockModeOverridesReadLockMode(LockMode readLock, LockMode writeLock, boolean success) {
        if (success) {
            GammaTxnFactory txFactory = stm.newTxnFactoryBuilder()
                    .setReadLockMode(readLock)
                    .setWriteLockMode(writeLock)
                    .newTransactionFactory();

            assertEquals(readLock, txFactory.getConfiguration().getReadLockMode());
            assertEquals(writeLock, txFactory.getConfiguration().getWriteLockMode());
        } else {
            try {
                stm.newTxnFactoryBuilder()
                        .setReadLockMode(readLock)
                        .setWriteLockMode(writeLock)
                        .newTransactionFactory();
                fail();
            } catch (IllegalTxnFactoryException expected) {
            }
        }
    }

    @Test
    public void whenReadtrackingDisabled() {
        GammaTxnFactory txFactory = stm.newTxnFactoryBuilder()
                .setReadTrackingEnabled(false)
                .setBlockingAllowed(false)
                .newTransactionFactory();

        assertFalse(txFactory.getConfiguration().isReadTrackingEnabled());
    }

    @Test
    public void whenSpeculativeConfigEnabled() {
        GammaTxnFactory txFactory = stm.newTxnFactoryBuilder()
                .setDirtyCheckEnabled(false)
                .setSpeculative(true)
                .newTransactionFactory();

        GammaTxnConfig configuration = txFactory.getConfiguration();
        assertFalse(configuration.getSpeculativeConfiguration().fat);
        assertTrue(configuration.isSpeculative());
    }

    @Test
    public void whenWriteSkewNotAllowed() {
        GammaTxnFactory txFactory = stm.newTxnFactoryBuilder()
                .setSpeculative(true)
                .setIsolationLevel(IsolationLevel.Serializable)
                .newTransactionFactory();


        GammaTxnConfig configuration = txFactory.getConfiguration();
        assertTrue(configuration.getSpeculativeConfiguration().fat);
        assertTrue(configuration.isSpeculative());
    }

    @Test
    public void whenSerializedThenFatTransaction() {
        GammaTxnFactory txFactory = stm.newTxnFactoryBuilder()
                .setSpeculative(true)
                .setDirtyCheckEnabled(false)
                .setIsolationLevel(IsolationLevel.Serializable)
                .newTransactionFactory();

        GammaTxn tx = txFactory.newTransaction();
        assertTrue(tx instanceof FatMonoGammaTxn);
    }

    @Test
    public void whenSnapshotIsolationLevelThenLeanTransaction() {
        GammaTxnFactory txFactory = stm.newTxnFactoryBuilder()
                .setSpeculative(true)
                .setDirtyCheckEnabled(false)
                .setIsolationLevel(IsolationLevel.Snapshot)
                .newTransactionFactory();

        GammaTxn tx = txFactory.newTransaction();
        assertEquals(TRANSACTIONTYPE_LEAN_MONO, tx.transactionType);
    }

     @Test
    public void whenReadonlyThenFatTransaction() {
        GammaTxnFactory txFactory = stm.newTxnFactoryBuilder()
                .setSpeculative(true)
                .setReadonly(true)
                .setDirtyCheckEnabled(false)
                .newTransactionFactory();

        GammaTxn tx = txFactory.newTransaction();
        assertEquals(TRANSACTIONTYPE_FAT_MONO,tx.transactionType);
    }
}

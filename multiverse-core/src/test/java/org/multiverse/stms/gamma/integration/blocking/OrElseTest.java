package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.callables.TxnCallable;
import org.multiverse.api.callables.TxnLongCallable;
import org.multiverse.api.exceptions.TxnMandatoryException;
import org.multiverse.api.references.TxnLong;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.multiverse.api.StmUtils.newTxnLong;
import static org.multiverse.api.StmUtils.retry;

/**
 * @author Peter Veentjer
 */
public class OrElseTest {

    @Test(expected = TxnMandatoryException.class)
    public void whenCalledWithoutTransaction_thenTxnMandatoryException() {
        TxnCallable callable = mock(TxnCallable.class);

        StmUtils.atomic(callable, callable);
    }

    @Test
    public void whenEitherBranchIsSuccess() {
        final TxnLong ref1 = newTxnLong(1);
        final TxnLong ref2 = newTxnLong(0);

        long value = StmUtils.atomic(new TxnLongCallable() {
            @Override
            public long call(Txn tx) throws Exception {
                return StmUtils.atomic(new GetCallable(ref1), new GetCallable(ref2));
            }
        });

        assertEquals(1, value);
    }

    class GetCallable implements TxnLongCallable {
        private final TxnLong ref;

        GetCallable(TxnLong ref) {
            this.ref = ref;
        }

        @Override
        public long call(Txn tx) throws Exception {
            if (ref.get() == 0) {
                retry();
            }

            return ref.get();
        }
    }

    @Test
    @Ignore
    public void whenOrElseBranchIsSuccess() {
        final TxnLong ref1 = newTxnLong(0);
        final TxnLong ref2 = newTxnLong(2);

        long value = StmUtils.atomic(new TxnLongCallable() {
            @Override
            public long call(Txn tx) throws Exception {
                return StmUtils.atomic(new GetCallable(ref1), new GetCallable(ref2));
            }
        });

        assertEquals(2, value);
    }

    @Test
    @Ignore
    public void whenBothBranchedBlock() {

    }
}

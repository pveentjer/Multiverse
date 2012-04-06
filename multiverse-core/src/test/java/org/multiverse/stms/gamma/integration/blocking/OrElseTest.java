package org.multiverse.stms.gamma.integration.blocking;

import org.junit.Ignore;
import org.junit.Test;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnClosure;
import org.multiverse.api.closures.TxnLongClosure;
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
        TxnClosure closure = mock(TxnClosure.class);

        StmUtils.atomic(closure, closure);
    }

    @Test
    public void whenEitherBranchIsSuccess() {
        final TxnLong ref1 = newTxnLong(1);
        final TxnLong ref2 = newTxnLong(0);

        long value = StmUtils.atomic(new TxnLongClosure() {
            @Override
            public long execute(Txn tx) throws Exception {
                return StmUtils.atomic(new GetClosure(ref1), new GetClosure(ref2));
            }
        });

        assertEquals(1, value);
    }

    class GetClosure implements TxnLongClosure {
        private final TxnLong ref;

        GetClosure(TxnLong ref) {
            this.ref = ref;
        }

        @Override
        public long execute(Txn tx) throws Exception {
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

        long value = StmUtils.atomic(new TxnLongClosure() {
            @Override
            public long execute(Txn tx) throws Exception {
                return StmUtils.atomic(new GetClosure(ref1), new GetClosure(ref2));
            }
        });

        assertEquals(2, value);
    }

    @Test
    @Ignore
    public void whenBothBranchedBlock() {

    }
}

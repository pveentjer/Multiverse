package org.multiverse.stms.gamma.transactionalobjects.txnref;

import org.multiverse.TestThread;
import org.multiverse.api.Txn;
import org.multiverse.api.closures.TxnVoidClosure;
import org.multiverse.api.predicates.Predicate;
import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;

public class TxnRefAwaitThread<T> extends TestThread {
    private final GammaTxnRef<T> ref;
    private final Predicate<T> predicate;

    public TxnRefAwaitThread(GammaTxnRef<T> ref, final T awaitValue) {
        this(ref, new Predicate<T>() {
            @Override
            public boolean evaluate(T current) {
                return current == awaitValue;
            }
        });
    }

    public TxnRefAwaitThread(GammaTxnRef ref, Predicate<T> predicate) {
        this.ref = ref;
        this.predicate = predicate;
    }

    @Override
    public void doRun() throws Exception {
        ref.getStm().getDefaultTxnExecutor().atomic(new TxnVoidClosure() {
            @Override
            public void execute(Txn tx) throws Exception {
                System.out.println("Starting wait and ref.value found: " + ref.get());
                ref.await(predicate);
                System.out.println("Finished wait and ref.value found: " + ref.get());
            }
        });
    }
}

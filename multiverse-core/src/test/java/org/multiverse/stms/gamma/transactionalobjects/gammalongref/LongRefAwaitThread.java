package org.multiverse.stms.gamma.transactionalobjects.gammalongref;

import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.predicates.LongPredicate;
import org.multiverse.stms.gamma.transactionalobjects.GammaLongRef;

public class LongRefAwaitThread extends TestThread {
    private final GammaLongRef ref;
    private final LongPredicate predicate;

    public LongRefAwaitThread(GammaLongRef ref, final long awaitValue) {
        this(ref, new LongPredicate() {
            @Override
            public boolean evaluate(long current) {
                return current == awaitValue;
            }
        });
    }

    public LongRefAwaitThread(GammaLongRef ref, LongPredicate predicate) {
        this.ref = ref;
        this.predicate = predicate;
    }

    @Override
    public void doRun() throws Exception {
        ref.getStm().getDefaultAtomicBlock().execute(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                System.out.println("Starting wait and ref.value found: " + ref.get());
                ref.await(predicate);
                System.out.println("Finished wait and ref.value found: " + ref.get());
            }
        });
    }
}

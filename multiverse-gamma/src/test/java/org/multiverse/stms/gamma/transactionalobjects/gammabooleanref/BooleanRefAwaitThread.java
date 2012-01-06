package org.multiverse.stms.gamma.transactionalobjects.gammabooleanref;

import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.predicates.BooleanPredicate;
import org.multiverse.stms.gamma.transactionalobjects.GammaBooleanRef;

public class BooleanRefAwaitThread extends TestThread {
    private final GammaBooleanRef ref;
    private final BooleanPredicate predicate;

    public BooleanRefAwaitThread(GammaBooleanRef ref, final boolean awaitValue) {
        this(ref, new BooleanPredicate() {
            @Override
            public boolean evaluate(boolean current) {
                return current == awaitValue;
            }
        });
    }

    public BooleanRefAwaitThread(GammaBooleanRef ref, BooleanPredicate predicate) {
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

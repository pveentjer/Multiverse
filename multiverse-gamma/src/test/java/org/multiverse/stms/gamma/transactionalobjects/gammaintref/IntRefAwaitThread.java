package org.multiverse.stms.gamma.transactionalobjects.gammaintref;

import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.predicates.IntPredicate;
import org.multiverse.stms.gamma.transactionalobjects.GammaIntRef;

public class IntRefAwaitThread extends TestThread {
    private final GammaIntRef ref;
    private final IntPredicate predicate;

    public IntRefAwaitThread(GammaIntRef ref, final int awaitValue) {
        this(ref, new IntPredicate() {
            @Override
            public boolean evaluate(int current) {
                return current == awaitValue;
            }
        });
    }

    public IntRefAwaitThread(GammaIntRef ref, IntPredicate predicate) {
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

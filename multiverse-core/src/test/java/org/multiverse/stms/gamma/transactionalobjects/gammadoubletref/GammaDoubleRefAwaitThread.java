package org.multiverse.stms.gamma.transactionalobjects.gammadoubletref;

import org.multiverse.TestThread;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;
import org.multiverse.api.predicates.DoublePredicate;
import org.multiverse.stms.gamma.transactionalobjects.GammaDoubleRef;

public class GammaDoubleRefAwaitThread extends TestThread {
    private final GammaDoubleRef ref;
    private final DoublePredicate predicate;

    public GammaDoubleRefAwaitThread(GammaDoubleRef ref, final double awaitValue) {
        this(ref, new DoublePredicate() {
            @Override
            public boolean evaluate(double current) {
                return current == awaitValue;
            }
        });
    }

    public GammaDoubleRefAwaitThread(GammaDoubleRef ref, DoublePredicate predicate) {
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

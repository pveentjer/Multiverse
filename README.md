h1. Multiverse Software Transactional Memory

A software transactional memory implementation for the JVM. Access (read and writes) to shared memory is done through
transactional references, that can be compared to the AtomicReferences of Java. Access to these references will be done
under A (atomicity), C (consistency), I (isolation) semantics.

h2. Example

    import org.multiverse.api.*;
    import org.multiverse.closures.*;
    import org.multiverse.api.references.*;
    import static org.multiverse.api.StmUtils.*;

    class Account{
        private final Ref<Date> lastModified = newRef(new Date());
        private final LongRef amount = newLongRef();

        public Date getLastModifiedDate(){
            return lastModified.get();
        }

        public long getAmount(){
            return amount.get();
        }

        public static void transfer(final Account from, final Account to, final long amount){
            execute(new AtomicVoidClosure()){
                public void execute(Transaction t){
                    Date date = new Date();

                    from.lastModified.set(date);
                    from.amount.dec(amount);

                    to.lastModified.set(date);
                    to.amount.inc(amount);
                }
            }
        }
    }

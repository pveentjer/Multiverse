Multiverse Software Transactional Memory
-------------------------

A software transactional memory implementation for the JVM. Access (read and writes) to shared memory is done through
transactional references, that can be compared to the AtomicReferences of Java. Access to these references will be done
under A (atomicity), C (consistency), I (isolation) semantics. For more information see <a href="http://multiverse.codehaus.org">multiverse.codehaus.org</a>

Example
-------------------------

    import org.multiverse.api.references.*;
    import static org.multiverse.api.StmUtils.*;
    
    import org.multiverse.stms.gamma.transactionalobjects.GammaTxnLong;
    import org.multiverse.stms.gamma.transactionalobjects.GammaTxnRef;

    import java.util.Date;

    public class Account{
        private final TxnRef<Date> lastModified;
        private final TxnLong amount;

        public Account(long amountValue){
            amount = new GammaTxnLong(amountValue);
            lastModified = new GammaTxnRef(new Date());
        }

        public Date getLastModifiedDate(){
            return lastModified.get();
        }

        public long getAmount(){
            return amount.get();
        }

        @Override
        public String toString() {
            return "Account{" +
                    "lastModified=" + lastModified.get() +
                    ", amount=" + amount.get() +
                    '}';
        }
        
        public static void transfer(final Account from, final Account to, final long amount){
            atomic(new Runnable()){
                public void run(){
                    Date date = new Date();

                    from.lastModified.set(date);
                    from.amount.decrement(amount);

                    to.lastModified.set(date);
                    to.amount.increment(amount);
                }
            }
        }
    }

    # And it can be called like this:

    Account account1 = new Account(10);
    Account account2 = new Account(20);
    Account.transfer(account1, account2, 5);


No instrumentation.
-------------------------
Multiverse doesn't rely on instrumentation, so is easy to integrate in existing projects.

package org.multiverse.collections;

import org.junit.Before;
import org.junit.Test;
import org.multiverse.api.Stm;
import org.multiverse.api.StmUtils;
import org.multiverse.api.Transaction;
import org.multiverse.api.closures.AtomicVoidClosure;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.multiverse.api.GlobalStmInstance.getGlobalStmInstance;
import static org.multiverse.api.ThreadLocalTransaction.clearThreadLocalTransaction;

public class NaiveTransactionalStack_addAllTest {

    private Stm stm;

    @Before
    public void setUp() {
        stm = getGlobalStmInstance();
        clearThreadLocalTransaction();
    }

    @Test
    public void whenNullCollectionAdded_thenNullPointerException() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                try {
                    stack.addAll(null);
                    fail();
                } catch (NullPointerException expected) {
                }

                assertEquals("[]", stack.toString());
            }
        });
    }

    @Test
    public void whenOneOfItemsIsNull() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm);

        try {
            StmUtils.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    List<String> list = new LinkedList<String>();
                    list.add("a");
                    list.add("b");
                    list.add(null);
                    list.add("d");

                    try {
                        stack.addAll(list);
                        fail();
                    } catch (NullPointerException expected) {
                    }

                    assertEquals("[b, a]", stack.toString());

                    throw new NullPointerException();
                }
            });
            fail();
        } catch (NullPointerException expected) {
        }

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                assertEquals("[]", stack.toString());
                assertEquals(0, stack.size());
            }
        });
    }

    @Test
    public void whenNonEmptyStackAndEmptyCollection() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                stack.push("1");
                stack.push("2");
                stack.addAll(new LinkedList<String>());

                assertEquals("[2, 1]", stack.toString());
                assertEquals(2, stack.size());
            }
        });
    }

    @Test
    public void whenBothEmpty() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                stack.addAll(new LinkedList<String>());

                assertEquals("[]", stack.toString());
                assertEquals(0, stack.size());
            }
        });
    }

    @Test
    public void whenEmptyStackEmptyAndNonEmptyCollection() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                List<String> list = Arrays.asList("1", "2");

                stack.addAll(list);

                assertEquals("[2, 1]", stack.toString());
                assertEquals(2, stack.size());
            }
        });
    }

    @Test
    public void whenBothNonEmpty() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                List<String> list = Arrays.asList("2", "1");

                stack.add("4");
                stack.add("3");
                stack.addAll(list);

                assertEquals("[1, 2, 3, 4]", stack.toString());
                assertEquals(4, stack.size());
            }
        });
    }

    @Test
    public void whenCapacityExceeded() {
        final NaiveTransactionalStack<String> stack = new NaiveTransactionalStack<String>(stm, 2);

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                stack.add("1");
            }
        });

        try {
            StmUtils.atomic(new AtomicVoidClosure() {
                @Override
                public void execute(Transaction tx) throws Exception {
                    List<String> c = Arrays.asList("2", "3");

                    try {
                        stack.addAll(c);
                        fail();
                    } catch (IllegalStateException expected) {

                    }

                    assertEquals("[2, 1]", stack.toString());
                    assertEquals(2, stack.size());
                    throw new IllegalStateException();
                }
            });
            fail();
        } catch (IllegalStateException expected) {

        }

        StmUtils.atomic(new AtomicVoidClosure() {
            @Override
            public void execute(Transaction tx) throws Exception {
                assertEquals(1, stack.size());
                assertEquals("[1]", stack.toString());
            }
        });
    }
}

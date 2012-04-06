package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.collections.TxnIterator;
import org.multiverse.api.collections.TxnStack;
import org.multiverse.api.references.IntRef;
import org.multiverse.api.references.Ref;

import java.util.NoSuchElementException;

import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public final class NaiveTxnStack<E> extends AbstractTxnCollection<E> implements TxnStack<E> {

    private final int capacity;
    private final Ref<Node<E>> head;
    private final IntRef size;

    public NaiveTxnStack(Stm stm) {
        this(stm, Integer.MAX_VALUE);
    }

    public NaiveTxnStack(Stm stm, int capacity) {
        super(stm);

        if (capacity < 0) {
            throw new IllegalArgumentException();
        }

        this.capacity = capacity;
        this.head = stm.getDefaultRefFactory().newRef(null);
        this.size = stm.getDefaultRefFactory().newIntRef(0);
    }

    @Override
    public int size(Txn tx) {
        return size.get(tx);
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void clear(Txn tx) {
        int s = size.get(tx);
        if (s == 0) {
            return;
        }

        size.set(tx, 0);
        head.set(tx, null);
    }

    @Override
    public boolean offer(E item) {
        return offer(getThreadLocalTxn(), item);
    }

    @Override
    public boolean offer(Txn tx, E item) {
        if (capacity == size(tx)) {
            return false;
        }

        push(tx, item);
        return true;
    }

    @Override
    public E poll() {
        return poll(getThreadLocalTxn());
    }

    @Override
    public E poll(Txn tx) {
        if (size.get(tx) == 0) {
            return null;
        }

        return pop(tx);
    }

    @Override
    public E peek() {
        return peek(getThreadLocalTxn());
    }

    @Override
    public E peek(Txn tx) {
        Node<E> h = head.get(tx);
        return h == null ? null : h.value;
    }

    @Override
    public void push(E item) {
        push(getThreadLocalTxn(), item);
    }

    @Override
    public void push(Txn tx, E item) {
        if (item == null) {
            throw new NullPointerException();
        }

        if (size.get(tx) == capacity) {
            tx.retry();
        }

        head.set(tx, new Node<E>(head.get(tx), item));
        size.increment(tx);
    }

    @Override
    public E pop() {
        return pop(getThreadLocalTxn());
    }

    @Override
    public E pop(Txn tx) {
        if (size.get(tx) == 0) {
            tx.retry();
        }

        Node<E> node = head.get(tx);
        head.set(tx, node.next);
        size.decrement(tx);
        return node.value;
    }

    @Override
    public boolean add(Txn tx, E e) {
        if (!offer(tx, e)) {
            throw new IllegalStateException("NaiveTxnStack full");
        }

        return true;
    }

    @Override
    public TxnIterator<E> iterator(Txn tx) {
        return new It<E>(stm, head.get(tx));
    }

    @Override
    public boolean contains(Txn tx, Object o) {
        if (o == null) {
            return false;
        }

        int s = size.get(tx);

        if (s == 0) {
            return false;
        }

        Node<E> node = head.get();
        while (node != null) {
            if(node.value.equals(o)){
                return true;
            }
            node = node.next;
        }
        return false;
    }

    @Override
    public boolean remove(Txn tx, Object o) {
        throw new UnsupportedOperationException();
    }

    static class It<E> extends AbstractTxnIterator<E> {
        final Ref<Node<E>> node;

        It(Stm stm, Node<E> node) {
            this.node = stm.getDefaultRefFactory().newRef(node);
        }

        @Override
        public boolean hasNext(Txn tx) {
            return node.get() != null;
        }

        @Override
        public E next(Txn tx) {
            Node<E> n = node.get(tx);

            if (n == null) {
                throw new NoSuchElementException();
            }

            E value = n.value;
            node.set(tx, n.next);
            return value;
        }

        @Override
        public void remove(Txn tx) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString(Txn tx) {
        int s = size.get(tx);
        if (s == 0) {
            return "[]";
        }

        StringBuffer sb = new StringBuffer("[");
        Node<E> node = head.get();

        while (node != null) {
            sb.append(node.value);
            node = node.next;
            if (node != null) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }

    static class Node<E> {
        final Node<E> next;
        final E value;

        Node(Node<E> next, E value) {
            this.next = next;
            this.value = value;
        }
    }
}

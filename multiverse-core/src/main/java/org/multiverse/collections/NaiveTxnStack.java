package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.collections.TxnIterator;
import org.multiverse.api.collections.TxnStack;
import org.multiverse.api.references.TxnInteger;
import org.multiverse.api.references.TxnRef;

import java.util.NoSuchElementException;

import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

public final class NaiveTxnStack<E> extends AbstractTxnCollection<E> implements TxnStack<E> {

    private final int capacity;
    private final TxnRef<Node<E>> head;
    private final TxnInteger size;

    public NaiveTxnStack(Stm stm) {
        this(stm, Integer.MAX_VALUE);
    }

    public NaiveTxnStack(Stm stm, int capacity) {
        super(stm);

        if (capacity < 0) {
            throw new IllegalArgumentException();
        }

        this.capacity = capacity;
        this.head = stm.getDefaultRefFactory().newTxnRef(null);
        this.size = stm.getDefaultRefFactory().newTxnInteger(0);
    }

    @Override
    public int size(Txn txn) {
        return size.get(txn);
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public void clear(Txn txn) {
        int s = size.get(txn);
        if (s == 0) {
            return;
        }

        size.set(txn, 0);
        head.set(txn, null);
    }

    @Override
    public boolean offer(E item) {
        return offer(getThreadLocalTxn(), item);
    }

    @Override
    public boolean offer(Txn txn, E item) {
        if (capacity == size(txn)) {
            return false;
        }

        push(txn, item);
        return true;
    }

    @Override
    public E poll() {
        return poll(getThreadLocalTxn());
    }

    @Override
    public E poll(Txn txn) {
        if (size.get(txn) == 0) {
            return null;
        }

        return pop(txn);
    }

    @Override
    public E peek() {
        return peek(getThreadLocalTxn());
    }

    @Override
    public E peek(Txn txn) {
        Node<E> h = head.get(txn);
        return h == null ? null : h.value;
    }

    @Override
    public void push(E item) {
        push(getThreadLocalTxn(), item);
    }

    @Override
    public void push(Txn txn, E item) {
        if (item == null) {
            throw new NullPointerException();
        }

        if (size.get(txn) == capacity) {
            txn.retry();
        }

        head.set(txn, new Node<E>(head.get(txn), item));
        size.increment(txn);
    }

    @Override
    public E pop() {
        return pop(getThreadLocalTxn());
    }

    @Override
    public E pop(Txn txn) {
        if (size.get(txn) == 0) {
            txn.retry();
        }

        Node<E> node = head.get(txn);
        head.set(txn, node.next);
        size.decrement(txn);
        return node.value;
    }

    @Override
    public boolean add(Txn txn, E e) {
        if (!offer(txn, e)) {
            throw new IllegalStateException("NaiveTxnStack full");
        }

        return true;
    }

    @Override
    public TxnIterator<E> iterator(Txn txn) {
        return new It<E>(stm, head.get(txn));
    }

    @Override
    public boolean contains(Txn txn, Object o) {
        if (o == null) {
            return false;
        }

        int s = size.get(txn);

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
    public boolean remove(Txn txn, Object o) {
        throw new UnsupportedOperationException();
    }

    static class It<E> extends AbstractTxnIterator<E> {
        final TxnRef<Node<E>> node;

        It(Stm stm, Node<E> node) {
            this.node = stm.getDefaultRefFactory().newTxnRef(node);
        }

        @Override
        public boolean hasNext(Txn txn) {
            return node.get() != null;
        }

        @Override
        public E next(Txn txn) {
            Node<E> n = node.get(txn);

            if (n == null) {
                throw new NoSuchElementException();
            }

            E value = n.value;
            node.set(txn, n.next);
            return value;
        }

        @Override
        public void remove(Txn txn) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString(Txn txn) {
        int s = size.get(txn);
        if (s == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
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

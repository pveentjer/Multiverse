package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.collections.TransactionalCollection;
import org.multiverse.api.collections.TransactionalIterator;
import org.multiverse.api.collections.TransactionalStack;
import org.multiverse.api.references.IntRef;
import org.multiverse.api.references.Ref;

import java.util.NoSuchElementException;

import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;

public final class NaiveTransactionalStack<E> extends AbstractTransactionalCollection<E> implements TransactionalStack<E> {

    private final int capacity;
    private final Ref<Node<E>> head;
    private final IntRef size;

    public NaiveTransactionalStack(Stm stm) {
        this(stm, Integer.MAX_VALUE);
    }

    public NaiveTransactionalStack(Stm stm, int capacity) {
        super(stm);

        if (capacity < 0) {
            throw new IllegalArgumentException();
        }

        this.capacity = capacity;
        this.head = stm.getDefaultRefFactory().newRef(null);
        this.size = stm.getDefaultRefFactory().newIntRef(0);
    }

    @Override
    public int size(Transaction tx) {
        return size.get(tx);
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public void clear(Transaction tx) {
        int s = size.get(tx);
        if (s == 0) {
            return;
        }

        size.set(tx, 0);
        head.set(tx, null);
    }

    @Override
    public boolean offer(E item) {
        return offer(getThreadLocalTransaction(), item);
    }

    @Override
    public boolean offer(Transaction tx, E item) {
        if (capacity == size(tx)) {
            return false;
        }

        push(tx, item);
        return true;
    }

    @Override
    public E poll() {
        return poll(getThreadLocalTransaction());
    }

    @Override
    public E poll(Transaction tx) {
        if (size.get(tx) == 0) {
            return null;
        }

        return pop(tx);
    }

    @Override
    public E peek() {
        return peek(getThreadLocalTransaction());
    }

    @Override
    public E peek(Transaction tx) {
        Node<E> h = head.get(tx);
        return h == null ? null : h.value;
    }

    @Override
    public void push(E item) {
        push(getThreadLocalTransaction(), item);
    }

    @Override
    public void push(Transaction tx, E item) {
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
        return pop(getThreadLocalTransaction());
    }

    @Override
    public E pop(Transaction tx) {
        if (size.get(tx) == 0) {
            tx.retry();
        }

        Node<E> node = head.get(tx);
        head.set(tx, node.next);
        size.decrement(tx);
        return node.value;
    }

    @Override
    public boolean add(Transaction tx, E e) {
        if (!offer(tx, e)) {
            throw new IllegalStateException("NaiveTransactionalStack full");
        }

        return true;
    }

    @Override
    public TransactionalIterator<E> iterator(Transaction tx) {
        return new It<E>(stm, head.get(tx));
    }

    @Override
    public boolean contains(Transaction tx, Object o) {
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
    public boolean remove(Transaction tx, Object o) {
        throw new UnsupportedOperationException();
    }

    static class It<E> extends AbstractTransactionalIterator<E> {
        final Ref<Node<E>> node;

        It(Stm stm, Node<E> node) {
            this.node = stm.getDefaultRefFactory().newRef(node);
        }

        @Override
        public boolean hasNext(Transaction tx) {
            return node.get() != null;
        }

        @Override
        public E next(Transaction tx) {
            Node<E> n = node.get(tx);

            if (n == null) {
                throw new NoSuchElementException();
            }

            E value = n.value;
            node.set(tx, n.next);
            return value;
        }

        @Override
        public void remove(Transaction tx) {
            throw new UnsupportedOperationException();
        }
    }

    @Override
    public String toString(Transaction tx) {
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

    @Override
    public TransactionalCollection<E> buildNew() {
        return new NaiveTransactionalStack(stm);
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

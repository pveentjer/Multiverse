package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.collections.TransactionalCollection;
import org.multiverse.api.collections.TransactionalDeque;
import org.multiverse.api.collections.TransactionalIterator;
import org.multiverse.api.collections.TransactionalList;
import org.multiverse.api.exceptions.TodoException;
import org.multiverse.api.references.IntRef;
import org.multiverse.api.references.Ref;
import org.multiverse.api.references.RefFactory;

import java.util.NoSuchElementException;

import static org.multiverse.api.ThreadLocalTransaction.getRequiredThreadLocalTransaction;
import static org.multiverse.api.ThreadLocalTransaction.getThreadLocalTransaction;

/**
 * A LinkedList implementation that also acts as a TransactionalQueue, TransactionalDeque.
 *
 * @param <E>
 */
public final class NaiveTransactionalLinkedList<E> extends AbstractTransactionalCollection<E>
        implements TransactionalDeque<E>, TransactionalList<E> {

    private final int capacity;
    private final IntRef size;
    private final Ref<Entry<E>> head;
    private final Ref<Entry<E>> tail;

    public NaiveTransactionalLinkedList(Stm stm) {
        this(stm, Integer.MAX_VALUE);
    }

    public NaiveTransactionalLinkedList(Stm stm, int capacity) {
        super(stm);

        if (capacity < 0) {
            throw new IllegalArgumentException();
        }

        this.capacity = capacity;
        this.size = stm.getDefaultRefFactory().newIntRef(0);
        this.head = stm.getDefaultRefFactory().newRef(null);
        this.tail = stm.getDefaultRefFactory().newRef(null);
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public E set(int index, E element) {
        return set(getThreadLocalTransaction(), index, element);
    }

    @Override
    public E set(Transaction tx, int index, E element) {
        return entry(tx, index).value.getAndSet(tx, element);
    }

    @Override
    public int size(Transaction tx) {
        return size.get(tx);
    }

    @Override
    public int indexOf(Object item) {
        return indexOf(getThreadLocalTransaction(), item);
    }

    @Override
    public int indexOf(Transaction tx, Object item) {
        if (item == null) {
            return -1;
        }

        int index = 0;
        Entry<E> node = head.get(tx);
        while (node != null) {
            if (node.value.get(tx).equals(item)) {
                return index;
            }
            node = node.next.get(tx);
            index++;
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object item) {
        return lastIndexOf(getThreadLocalTransaction(), item);
    }

    @Override
    public int lastIndexOf(Transaction tx, Object item) {
        if (item == null) {
            return -1;
        }

        int index = size.get(tx) - 1;
        Entry<E> node = tail.get(tx);
        while (node != null) {
            if (node.value.get(tx).equals(item)) {
                return index;
            }
            node = node.previous.get(tx);
            index--;
        }

        return -1;
    }

    private Entry<E> entry(Transaction tx, int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }

        int s = size.get(tx);
        if (index >= s) {
            throw new IndexOutOfBoundsException();
        }

        if (index < (s >> 1)) {
            int i = 0;
            Entry<E> node = head.get(tx);
            while (true) {
                if (i == index) {
                    return node;
                }
                node = node.next.get(tx);
                i++;
            }
        } else {
            int i = s - 1;
            Entry<E> node = tail.get(tx);
            while (true) {
                if (i == index) {
                    return node;
                }
                node = node.previous.get(tx);
                i--;
            }
        }
    }

    @Override
    public boolean contains(Transaction tx, Object o) {
        return indexOf(tx, o) != -1;
    }

    @Override
    public boolean remove(Transaction tx, Object o) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clear(Transaction tx) {
        if (size.get(tx) == 0) {
            return;
        }

        size.set(tx, 0);
        head.set(tx, null);
        tail.set(tx, null);
    }

    // ==================== needs sorting =====================================


    @Override
    public E element() {
        return element(getThreadLocalTransaction());
    }

    @Override
    public E element(Transaction tx) {
        return getFirst(tx);
    }

    @Override
    public E pop() {
        return pop(getThreadLocalTransaction());
    }

    @Override
    public E pop(Transaction tx) {
        return removeFirst(tx);
    }

    @Override
    public void push(E e) {
        push(getThreadLocalTransaction(), e);
    }

    @Override
    public void push(Transaction tx, E e) {
        addFirst(tx, e);
    }

    // =============== remove ==============

    @Override
    public E remove(int index) {
        return remove(getThreadLocalTransaction(), index);
    }

    @Override
    public E remove(Transaction tx, int index) {
        Entry entry = entry(tx, index);
        throw new UnsupportedOperationException();
    }

    @Override
    public E removeFirst() {
        return removeFirst(getThreadLocalTransaction());
    }

    @Override
    public E removeFirst(Transaction tx) {
        E element = pollFirst(tx);
        if (element == null) {
            throw new NoSuchElementException("NaiveTransactionalLinkedList is empty");
        }
        return element;
    }

    @Override
    public E removeLast() {
        return removeLast(getThreadLocalTransaction());
    }

    @Override
    public E removeLast(Transaction tx) {
        E element = pollLast(tx);
        if (element == null) {
            throw new NoSuchElementException("NaiveTransactionalLinkedList is empty");
        }
        return element;
    }

    @Override
    public E remove() {
        return remove(getThreadLocalTransaction());
    }

    @Override
    public E remove(Transaction tx) {
        return removeFirst(tx);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return removeFirstOccurrence(getThreadLocalTransaction(), o);
    }

    @Override
    public boolean removeFirstOccurrence(Transaction tx, Object o) {
        throw new TodoException();
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return removeLastOccurrence(getThreadLocalTransaction(), o);
    }

    @Override
    public boolean removeLastOccurrence(Transaction tx, Object o) {
        throw new TodoException();
    }


    // =============== gets ==============

    @Override
    public E getFirst() {
        return getFirst(getThreadLocalTransaction());
    }

    @Override
    public E getFirst(Transaction tx) {
        E result = pollFirst(tx);
        if (result == null) {
            throw new NoSuchElementException("NaiveTransactionalLinkedList is empty");
        }
        return result;
    }

    @Override
    public E getLast() {
        return getLast(getThreadLocalTransaction());
    }

    @Override
    public E getLast(Transaction tx) {
        E result = pollLast(tx);
        if (result == null) {
            throw new NoSuchElementException("NaiveTransactionalLinkedList is empty");
        }
        return result;
    }

    @Override
    public E get(int index) {
        return get(getThreadLocalTransaction(), index);
    }

    @Override
    public E get(Transaction tx, int index) {
        return entry(tx, index).value.get(tx);
    }

    // ============== adds ================

    @Override
    public void addFirst(E e) {
        addFirst(getThreadLocalTransaction(), e);
    }

    @Override
    public void addFirst(Transaction tx, E e) {
        if (!offerFirst(tx, e)) {
            throw new IllegalStateException("NaiveTransactionalLinkedList full");
        }
    }

    @Override
    public void addLast(E e) {
        addLast(getThreadLocalTransaction(), e);
    }

    @Override
    public void addLast(Transaction tx, E e) {
        if (!offerLast(tx, e)) {
            throw new IllegalStateException("NaiveTransactionalLinkedList full");
        }
    }

    @Override
    public boolean add(Transaction tx, E e) {
        if (!offer(tx, e)) {
            throw new IllegalStateException("NaiveTransactionalLinkedList full");
        }

        return true;
    }

    // ================ puts ==========================

    @Override
    public void putFirst(E item) {
        putFirst(getThreadLocalTransaction(), item);
    }

    @Override
    public void putFirst(Transaction tx, E item) {
        if (!offerFirst(tx, item)) {
            tx.retry();
        }
    }

    @Override
    public void put(E item) {
        put(getThreadLocalTransaction(), item);
    }

    @Override
    public void put(Transaction tx, E item) {
        putLast(tx, item);
    }

    @Override
    public void putLast(E item) {
        putLast(getRequiredThreadLocalTransaction(), item);
    }

    @Override
    public void putLast(Transaction tx, E item) {
        if (!offerLast(tx, item)) {
            tx.retry();
        }
    }

    // ================== takes ===============================

    @Override
    public E take() {
        return take(getThreadLocalTransaction());
    }

    @Override
    public E take(Transaction tx) {
        return takeLast(tx);
    }

    @Override
    public E takeFirst() {
        return takeFirst(getThreadLocalTransaction());
    }

    @Override
    public E takeFirst(Transaction tx) {
        E item = pollFirst(tx);
        if (item == null) {
            tx.retry();
        }
        return item;
    }

    @Override
    public E takeLast() {
        return takeLast(getThreadLocalTransaction());
    }

    @Override
    public E takeLast(Transaction tx) {
        E item = pollLast(tx);
        if (item == null) {
            tx.retry();
        }

        return item;
    }

    // ================== offers ========================

    @Override
    public boolean offerFirst(E e) {
        return offerFirst(getThreadLocalTransaction(), e);
    }

    @Override
    public boolean offerFirst(Transaction tx, E item) {
        if (item == null) {
            throw new NullPointerException();
        }

        int s = size.get(tx);
        if (s == capacity) {
            return false;
        }

        Entry<E> node = new Entry<E>(defaultRefFactory, item);
        if (s == 0) {
            head.set(tx, node);
            tail.set(tx, node);
        } else {
            node.next.set(tx, head.get(tx));
            head.get(tx).previous.set(tx, node);
            head.set(tx, node);
        }
        size.increment(tx);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        return offerLast(getThreadLocalTransaction(), e);
    }

    @Override
    public boolean offerLast(Transaction tx, E item) {
        if (item == null) {
            throw new NullPointerException();
        }

        int s = size.get(tx);
        if (s == capacity) {
            return false;
        }

        Entry<E> node = new Entry<E>(defaultRefFactory, item);
        if (s == 0) {
            head.set(tx, node);
            tail.set(tx, node);
        } else {
            node.previous.set(tx, tail.get(tx));
            tail.get(tx).next.set(tx, node);
            tail.set(tx, node);
        }
        size.increment(tx);
        return true;
    }

    @Override
    public boolean offer(E item) {
        return offer(getThreadLocalTransaction(), item);
    }

    @Override
    public boolean offer(Transaction tx, E item) {
        return offerLast(tx, item);
    }

    // ================ polls =======================

    @Override
    public E pollFirst() {
        return pollFirst(getThreadLocalTransaction());
    }

    @Override
    public E pollFirst(Transaction tx) {
        int s = size.get(tx);

        if (s == 0) {
            return null;
        }

        E item;
        if (s == 1) {
            item = tail.get(tx).value.get(tx);
            head.set(tx, null);
            tail.set(tx, null);
        } else {
            Entry<E> oldHead = head.get(tx);
            item = oldHead.value.get(tx);
            Entry<E> newHead = oldHead.next.get(tx);
            head.set(tx, newHead);
            newHead.previous.set(tx, null);
        }
        size.decrement(tx);
        return item;
    }

    @Override
    public E pollLast() {
        return pollLast(getThreadLocalTransaction());
    }

    @Override
    public E pollLast(Transaction tx) {
        int s = size.get(tx);

        if (s == 0) {
            return null;
        }

        E item;
        if (s == 1) {
            item = head.get(tx).value.get(tx);
            head.set(tx, null);
            tail.set(tx, null);
        } else {
            Entry<E> oldTail = tail.get(tx);
            item = oldTail.value.get(tx);
            Entry<E> newTail = oldTail.previous.get(tx);
            tail.set(tx, newTail);
            newTail.next.set(tx, null);
        }
        size.decrement(tx);
        return item;
    }

    @Override
    public E poll() {
        return poll(getThreadLocalTransaction());
    }

    @Override
    public E poll(Transaction tx) {
        return pollLast(tx);
    }

    // =============== peeks =================

    @Override
    public E peekFirst() {
        return peekFirst(getThreadLocalTransaction());
    }

    @Override
    public E peekFirst(Transaction tx) {
        Entry<E> h = head.get(tx);
        return h == null ? null : h.value.get(tx);
    }

    @Override
    public E peekLast() {
        return peekLast(getThreadLocalTransaction());
    }

    @Override
    public E peekLast(Transaction tx) {
        Entry<E> t = tail.get(tx);
        return t == null ? null : t.value.get(tx);
    }

    @Override
    public E peek() {
        return peek(getThreadLocalTransaction());
    }

    @Override
    public E peek(Transaction tx) {
        return peekFirst(tx);
    }

    // ================ misc ==========================

    @Override
    public TransactionalIterator<E> iterator(Transaction tx) {
        throw new TodoException();
    }

    @Override
    public TransactionalIterator<E> descendingIterator() {
        return descendingIterator(getThreadLocalTransaction());
    }

    @Override
    public TransactionalIterator<E> descendingIterator(Transaction tx) {
        throw new TodoException();
    }

    // ================ misc ==========================


    @Override
    public String toString(Transaction tx) {
        int s = size(tx);
        if (s == 0) {
            return "[]";
        }

        StringBuffer sb = new StringBuffer();
        sb.append('[');
        Entry<E> node = head.get(tx);
        do {
            sb.append(node.value.get(tx));
            node = node.next.get(tx);
            if (node != null) {
                sb.append(", ");
            }
        } while (node != null);
        sb.append(']');
        return sb.toString();
    }

    static class Entry<E> {
        private final Ref<Entry<E>> next;
        private final Ref<Entry<E>> previous;
        private final Ref<E> value;

        Entry(RefFactory refFactory, E value) {
            this.next = refFactory.newRef(null);
            this.previous = refFactory.newRef(null);
            this.value = refFactory.newRef(value);
        }
    }
}

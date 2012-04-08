package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.collections.TxnDeque;
import org.multiverse.api.collections.TxnIterator;
import org.multiverse.api.collections.TxnList;
import org.multiverse.api.exceptions.TodoException;
import org.multiverse.api.references.TxnInteger;
import org.multiverse.api.references.TxnRef;
import org.multiverse.api.references.TxnRefFactory;

import java.util.NoSuchElementException;

import static org.multiverse.api.TxnThreadLocal.getRequiredThreadLocalTxn;
import static org.multiverse.api.TxnThreadLocal.getThreadLocalTxn;

/**
 * A LinkedList implementation that also acts as a TxnQueue, TxnDeque.
 *
 * @param <E>
 */
public final class NaiveTxnLinkedList<E> extends AbstractTxnCollection<E>
        implements TxnDeque<E>, TxnList<E> {

    private final int capacity;
    private final TxnInteger size;
    private final TxnRef<Entry<E>> head;
    private final TxnRef<Entry<E>> tail;

    public NaiveTxnLinkedList(Stm stm) {
        this(stm, Integer.MAX_VALUE);
    }

    public NaiveTxnLinkedList(Stm stm, int capacity) {
        super(stm);

        if (capacity < 0) {
            throw new IllegalArgumentException();
        }

        this.capacity = capacity;
        this.size = stm.getDefaultRefFactory().newTxnInteger(0);
        this.head = stm.getDefaultRefFactory().newTxnRef(null);
        this.tail = stm.getDefaultRefFactory().newTxnRef(null);
    }

    @Override
    public int getCapacity() {
        return capacity;
    }

    @Override
    public E set(int index, E element) {
        return set(getThreadLocalTxn(), index, element);
    }

    @Override
    public E set(Txn txn, int index, E element) {
        return entry(txn, index).value.getAndSet(txn, element);
    }

    @Override
    public int size(Txn txn) {
        return size.get(txn);
    }

    @Override
    public int indexOf(Object item) {
        return indexOf(getThreadLocalTxn(), item);
    }

    @Override
    public int indexOf(Txn txn, Object item) {
        if (item == null) {
            return -1;
        }

        int index = 0;
        Entry<E> node = head.get(txn);
        while (node != null) {
            if (node.value.get(txn).equals(item)) {
                return index;
            }
            node = node.next.get(txn);
            index++;
        }

        return -1;
    }

    @Override
    public int lastIndexOf(Object item) {
        return lastIndexOf(getThreadLocalTxn(), item);
    }

    @Override
    public int lastIndexOf(Txn txn, Object item) {
        if (item == null) {
            return -1;
        }

        int index = size.get(txn) - 1;
        Entry<E> node = tail.get(txn);
        while (node != null) {
            if (node.value.get(txn).equals(item)) {
                return index;
            }
            node = node.previous.get(txn);
            index--;
        }

        return -1;
    }

    private Entry<E> entry(Txn txn, int index) {
        if (index < 0) {
            throw new IndexOutOfBoundsException();
        }

        int s = size.get(txn);
        if (index >= s) {
            throw new IndexOutOfBoundsException();
        }

        if (index < (s >> 1)) {
            int i = 0;
            Entry<E> node = head.get(txn);
            while (true) {
                if (i == index) {
                    return node;
                }
                node = node.next.get(txn);
                i++;
            }
        } else {
            int i = s - 1;
            Entry<E> node = tail.get(txn);
            while (true) {
                if (i == index) {
                    return node;
                }
                node = node.previous.get(txn);
                i--;
            }
        }
    }

    @Override
    public boolean contains(Txn txn, Object o) {
        return indexOf(txn, o) != -1;
    }

    @Override
    public boolean remove(Txn txn, Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void clear(Txn txn) {
        if (size.get(txn) == 0) {
            return;
        }

        size.set(txn, 0);
        head.set(txn, null);
        tail.set(txn, null);
    }

    // ==================== needs sorting =====================================


    @Override
    public E element() {
        return element(getThreadLocalTxn());
    }

    @Override
    public E element(Txn txn) {
        return getFirst(txn);
    }

    @Override
    public E pop() {
        return pop(getThreadLocalTxn());
    }

    @Override
    public E pop(Txn txn) {
        return removeFirst(txn);
    }

    @Override
    public void push(E e) {
        push(getThreadLocalTxn(), e);
    }

    @Override
    public void push(Txn txn, E e) {
        addFirst(txn, e);
    }

    // =============== remove ==============

    @Override
    public E remove(int index) {
        return remove(getThreadLocalTxn(), index);
    }

    @Override
    public E remove(Txn txn, int index) {
        Entry entry = entry(txn, index);
        throw new UnsupportedOperationException();
    }

    @Override
    public E removeFirst() {
        return removeFirst(getThreadLocalTxn());
    }

    @Override
    public E removeFirst(Txn txn) {
        E element = pollFirst(txn);
        if (element == null) {
            throw new NoSuchElementException("NaiveTxnLinkedList is empty");
        }
        return element;
    }

    @Override
    public E removeLast() {
        return removeLast(getThreadLocalTxn());
    }

    @Override
    public E removeLast(Txn txn) {
        E element = pollLast(txn);
        if (element == null) {
            throw new NoSuchElementException("NaiveTxnLinkedList is empty");
        }
        return element;
    }

    @Override
    public E remove() {
        return remove(getThreadLocalTxn());
    }

    @Override
    public E remove(Txn txn) {
        return removeFirst(txn);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return removeFirstOccurrence(getThreadLocalTxn(), o);
    }

    @Override
    public boolean removeFirstOccurrence(Txn txn, Object o) {
        throw new TodoException();
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return removeLastOccurrence(getThreadLocalTxn(), o);
    }

    @Override
    public boolean removeLastOccurrence(Txn txn, Object o) {
        throw new TodoException();
    }


    // =============== gets ==============

    @Override
    public E getFirst() {
        return getFirst(getThreadLocalTxn());
    }

    @Override
    public E getFirst(Txn txn) {
        E result = pollFirst(txn);
        if (result == null) {
            throw new NoSuchElementException("NaiveTxnLinkedList is empty");
        }
        return result;
    }

    @Override
    public E getLast() {
        return getLast(getThreadLocalTxn());
    }

    @Override
    public E getLast(Txn txn) {
        E result = pollLast(txn);
        if (result == null) {
            throw new NoSuchElementException("NaiveTxnLinkedList is empty");
        }
        return result;
    }

    @Override
    public E get(int index) {
        return get(getThreadLocalTxn(), index);
    }

    @Override
    public E get(Txn txn, int index) {
        return entry(txn, index).value.get(txn);
    }

    // ============== adds ================

    @Override
    public void addFirst(E e) {
        addFirst(getThreadLocalTxn(), e);
    }

    @Override
    public void addFirst(Txn txn, E e) {
        if (!offerFirst(txn, e)) {
            throw new IllegalStateException("NaiveTxnLinkedList full");
        }
    }

    @Override
    public void addLast(E e) {
        addLast(getThreadLocalTxn(), e);
    }

    @Override
    public void addLast(Txn txn, E e) {
        if (!offerLast(txn, e)) {
            throw new IllegalStateException("NaiveTxnLinkedList full");
        }
    }

    @Override
    public boolean add(Txn txn, E e) {
        if (!offer(txn, e)) {
            throw new IllegalStateException("NaiveTxnLinkedList full");
        }

        return true;
    }

    // ================ puts ==========================

    @Override
    public void putFirst(E item) {
        putFirst(getThreadLocalTxn(), item);
    }

    @Override
    public void putFirst(Txn txn, E item) {
        if (!offerFirst(txn, item)) {
            txn.retry();
        }
    }

    @Override
    public void put(E item) {
        put(getThreadLocalTxn(), item);
    }

    @Override
    public void put(Txn txn, E item) {
        putLast(txn, item);
    }

    @Override
    public void putLast(E item) {
        putLast(getRequiredThreadLocalTxn(), item);
    }

    @Override
    public void putLast(Txn txn, E item) {
        if (!offerLast(txn, item)) {
            txn.retry();
        }
    }

    // ================== takes ===============================

    @Override
    public E take() {
        return take(getThreadLocalTxn());
    }

    @Override
    public E take(Txn txn) {
        return takeLast(txn);
    }

    @Override
    public E takeFirst() {
        return takeFirst(getThreadLocalTxn());
    }

    @Override
    public E takeFirst(Txn txn) {
        E item = pollFirst(txn);
        if (item == null) {
            txn.retry();
        }
        return item;
    }

    @Override
    public E takeLast() {
        return takeLast(getThreadLocalTxn());
    }

    @Override
    public E takeLast(Txn txn) {
        E item = pollLast(txn);
        if (item == null) {
            txn.retry();
        }

        return item;
    }

    // ================== offers ========================

    @Override
    public boolean offerFirst(E e) {
        return offerFirst(getThreadLocalTxn(), e);
    }

    @Override
    public boolean offerFirst(Txn txn, E item) {
        if (item == null) {
            throw new NullPointerException();
        }

        int s = size.get(txn);
        if (s == capacity) {
            return false;
        }

        Entry<E> node = new Entry<E>(defaultRefFactory, item);
        if (s == 0) {
            head.set(txn, node);
            tail.set(txn, node);
        } else {
            node.next.set(txn, head.get(txn));
            head.get(txn).previous.set(txn, node);
            head.set(txn, node);
        }
        size.increment(txn);
        return true;
    }

    @Override
    public boolean offerLast(E e) {
        return offerLast(getThreadLocalTxn(), e);
    }

    @Override
    public boolean offerLast(Txn txn, E item) {
        if (item == null) {
            throw new NullPointerException();
        }

        int s = size.get(txn);
        if (s == capacity) {
            return false;
        }

        Entry<E> node = new Entry<E>(defaultRefFactory, item);
        if (s == 0) {
            head.set(txn, node);
            tail.set(txn, node);
        } else {
            node.previous.set(txn, tail.get(txn));
            tail.get(txn).next.set(txn, node);
            tail.set(txn, node);
        }
        size.increment(txn);
        return true;
    }

    @Override
    public boolean offer(E item) {
        return offer(getThreadLocalTxn(), item);
    }

    @Override
    public boolean offer(Txn txn, E item) {
        return offerLast(txn, item);
    }

    // ================ polls =======================

    @Override
    public E pollFirst() {
        return pollFirst(getThreadLocalTxn());
    }

    @Override
    public E pollFirst(Txn txn) {
        int s = size.get(txn);

        if (s == 0) {
            return null;
        }

        E item;
        if (s == 1) {
            item = tail.get(txn).value.get(txn);
            head.set(txn, null);
            tail.set(txn, null);
        } else {
            Entry<E> oldHead = head.get(txn);
            item = oldHead.value.get(txn);
            Entry<E> newHead = oldHead.next.get(txn);
            head.set(txn, newHead);
            newHead.previous.set(txn, null);
        }
        size.decrement(txn);
        return item;
    }

    @Override
    public E pollLast() {
        return pollLast(getThreadLocalTxn());
    }

    @Override
    public E pollLast(Txn txn) {
        int s = size.get(txn);

        if (s == 0) {
            return null;
        }

        E item;
        if (s == 1) {
            item = head.get(txn).value.get(txn);
            head.set(txn, null);
            tail.set(txn, null);
        } else {
            Entry<E> oldTail = tail.get(txn);
            item = oldTail.value.get(txn);
            Entry<E> newTail = oldTail.previous.get(txn);
            tail.set(txn, newTail);
            newTail.next.set(txn, null);
        }
        size.decrement(txn);
        return item;
    }

    @Override
    public E poll() {
        return poll(getThreadLocalTxn());
    }

    @Override
    public E poll(Txn txn) {
        return pollLast(txn);
    }

    // =============== peeks =================

    @Override
    public E peekFirst() {
        return peekFirst(getThreadLocalTxn());
    }

    @Override
    public E peekFirst(Txn txn) {
        Entry<E> h = head.get(txn);
        return h == null ? null : h.value.get(txn);
    }

    @Override
    public E peekLast() {
        return peekLast(getThreadLocalTxn());
    }

    @Override
    public E peekLast(Txn txn) {
        Entry<E> t = tail.get(txn);
        return t == null ? null : t.value.get(txn);
    }

    @Override
    public E peek() {
        return peek(getThreadLocalTxn());
    }

    @Override
    public E peek(Txn txn) {
        return peekFirst(txn);
    }

    // ================ misc ==========================

    @Override
    public TxnIterator<E> iterator(Txn txn) {
        throw new TodoException();
    }

    @Override
    public TxnIterator<E> descendingIterator() {
        return descendingIterator(getThreadLocalTxn());
    }

    @Override
    public TxnIterator<E> descendingIterator(Txn txn) {
        throw new TodoException();
    }

    // ================ misc ==========================


    @Override
    public String toString(Txn txn) {
        int s = size(txn);
        if (s == 0) {
            return "[]";
        }

        StringBuffer sb = new StringBuffer();
        sb.append('[');
        Entry<E> node = head.get(txn);
        do {
            sb.append(node.value.get(txn));
            node = node.next.get(txn);
            if (node != null) {
                sb.append(", ");
            }
        } while (node != null);
        sb.append(']');
        return sb.toString();
    }

    static class Entry<E> {
        private final TxnRef<Entry<E>> next;
        private final TxnRef<Entry<E>> previous;
        private final TxnRef<E> value;

        Entry(TxnRefFactory refFactory, E value) {
            this.next = refFactory.newTxnRef(null);
            this.previous = refFactory.newTxnRef(null);
            this.value = refFactory.newTxnRef(value);
        }
    }
}

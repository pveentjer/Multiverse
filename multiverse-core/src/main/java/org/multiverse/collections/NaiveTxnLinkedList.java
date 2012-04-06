package org.multiverse.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Txn;
import org.multiverse.api.collections.TxnDeque;
import org.multiverse.api.collections.TxnIterator;
import org.multiverse.api.collections.TxnList;
import org.multiverse.api.exceptions.TodoException;
import org.multiverse.api.references.IntRef;
import org.multiverse.api.references.Ref;
import org.multiverse.api.references.RefFactory;

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
    private final IntRef size;
    private final Ref<Entry<E>> head;
    private final Ref<Entry<E>> tail;

    public NaiveTxnLinkedList(Stm stm) {
        this(stm, Integer.MAX_VALUE);
    }

    public NaiveTxnLinkedList(Stm stm, int capacity) {
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
        return set(getThreadLocalTxn(), index, element);
    }

    @Override
    public E set(Txn tx, int index, E element) {
        return entry(tx, index).value.getAndSet(tx, element);
    }

    @Override
    public int size(Txn tx) {
        return size.get(tx);
    }

    @Override
    public int indexOf(Object item) {
        return indexOf(getThreadLocalTxn(), item);
    }

    @Override
    public int indexOf(Txn tx, Object item) {
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
        return lastIndexOf(getThreadLocalTxn(), item);
    }

    @Override
    public int lastIndexOf(Txn tx, Object item) {
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

    private Entry<E> entry(Txn tx, int index) {
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
    public boolean contains(Txn tx, Object o) {
        return indexOf(tx, o) != -1;
    }

    @Override
    public boolean remove(Txn tx, Object o) {
        return false;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void clear(Txn tx) {
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
        return element(getThreadLocalTxn());
    }

    @Override
    public E element(Txn tx) {
        return getFirst(tx);
    }

    @Override
    public E pop() {
        return pop(getThreadLocalTxn());
    }

    @Override
    public E pop(Txn tx) {
        return removeFirst(tx);
    }

    @Override
    public void push(E e) {
        push(getThreadLocalTxn(), e);
    }

    @Override
    public void push(Txn tx, E e) {
        addFirst(tx, e);
    }

    // =============== remove ==============

    @Override
    public E remove(int index) {
        return remove(getThreadLocalTxn(), index);
    }

    @Override
    public E remove(Txn tx, int index) {
        Entry entry = entry(tx, index);
        throw new UnsupportedOperationException();
    }

    @Override
    public E removeFirst() {
        return removeFirst(getThreadLocalTxn());
    }

    @Override
    public E removeFirst(Txn tx) {
        E element = pollFirst(tx);
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
    public E removeLast(Txn tx) {
        E element = pollLast(tx);
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
    public E remove(Txn tx) {
        return removeFirst(tx);
    }

    @Override
    public boolean removeFirstOccurrence(Object o) {
        return removeFirstOccurrence(getThreadLocalTxn(), o);
    }

    @Override
    public boolean removeFirstOccurrence(Txn tx, Object o) {
        throw new TodoException();
    }

    @Override
    public boolean removeLastOccurrence(Object o) {
        return removeLastOccurrence(getThreadLocalTxn(), o);
    }

    @Override
    public boolean removeLastOccurrence(Txn tx, Object o) {
        throw new TodoException();
    }


    // =============== gets ==============

    @Override
    public E getFirst() {
        return getFirst(getThreadLocalTxn());
    }

    @Override
    public E getFirst(Txn tx) {
        E result = pollFirst(tx);
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
    public E getLast(Txn tx) {
        E result = pollLast(tx);
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
    public E get(Txn tx, int index) {
        return entry(tx, index).value.get(tx);
    }

    // ============== adds ================

    @Override
    public void addFirst(E e) {
        addFirst(getThreadLocalTxn(), e);
    }

    @Override
    public void addFirst(Txn tx, E e) {
        if (!offerFirst(tx, e)) {
            throw new IllegalStateException("NaiveTxnLinkedList full");
        }
    }

    @Override
    public void addLast(E e) {
        addLast(getThreadLocalTxn(), e);
    }

    @Override
    public void addLast(Txn tx, E e) {
        if (!offerLast(tx, e)) {
            throw new IllegalStateException("NaiveTxnLinkedList full");
        }
    }

    @Override
    public boolean add(Txn tx, E e) {
        if (!offer(tx, e)) {
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
    public void putFirst(Txn tx, E item) {
        if (!offerFirst(tx, item)) {
            tx.retry();
        }
    }

    @Override
    public void put(E item) {
        put(getThreadLocalTxn(), item);
    }

    @Override
    public void put(Txn tx, E item) {
        putLast(tx, item);
    }

    @Override
    public void putLast(E item) {
        putLast(getRequiredThreadLocalTxn(), item);
    }

    @Override
    public void putLast(Txn tx, E item) {
        if (!offerLast(tx, item)) {
            tx.retry();
        }
    }

    // ================== takes ===============================

    @Override
    public E take() {
        return take(getThreadLocalTxn());
    }

    @Override
    public E take(Txn tx) {
        return takeLast(tx);
    }

    @Override
    public E takeFirst() {
        return takeFirst(getThreadLocalTxn());
    }

    @Override
    public E takeFirst(Txn tx) {
        E item = pollFirst(tx);
        if (item == null) {
            tx.retry();
        }
        return item;
    }

    @Override
    public E takeLast() {
        return takeLast(getThreadLocalTxn());
    }

    @Override
    public E takeLast(Txn tx) {
        E item = pollLast(tx);
        if (item == null) {
            tx.retry();
        }

        return item;
    }

    // ================== offers ========================

    @Override
    public boolean offerFirst(E e) {
        return offerFirst(getThreadLocalTxn(), e);
    }

    @Override
    public boolean offerFirst(Txn tx, E item) {
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
        return offerLast(getThreadLocalTxn(), e);
    }

    @Override
    public boolean offerLast(Txn tx, E item) {
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
        return offer(getThreadLocalTxn(), item);
    }

    @Override
    public boolean offer(Txn tx, E item) {
        return offerLast(tx, item);
    }

    // ================ polls =======================

    @Override
    public E pollFirst() {
        return pollFirst(getThreadLocalTxn());
    }

    @Override
    public E pollFirst(Txn tx) {
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
        return pollLast(getThreadLocalTxn());
    }

    @Override
    public E pollLast(Txn tx) {
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
        return poll(getThreadLocalTxn());
    }

    @Override
    public E poll(Txn tx) {
        return pollLast(tx);
    }

    // =============== peeks =================

    @Override
    public E peekFirst() {
        return peekFirst(getThreadLocalTxn());
    }

    @Override
    public E peekFirst(Txn tx) {
        Entry<E> h = head.get(tx);
        return h == null ? null : h.value.get(tx);
    }

    @Override
    public E peekLast() {
        return peekLast(getThreadLocalTxn());
    }

    @Override
    public E peekLast(Txn tx) {
        Entry<E> t = tail.get(tx);
        return t == null ? null : t.value.get(tx);
    }

    @Override
    public E peek() {
        return peek(getThreadLocalTxn());
    }

    @Override
    public E peek(Txn tx) {
        return peekFirst(tx);
    }

    // ================ misc ==========================

    @Override
    public TxnIterator<E> iterator(Txn tx) {
        throw new TodoException();
    }

    @Override
    public TxnIterator<E> descendingIterator() {
        return descendingIterator(getThreadLocalTxn());
    }

    @Override
    public TxnIterator<E> descendingIterator(Txn tx) {
        throw new TodoException();
    }

    // ================ misc ==========================


    @Override
    public String toString(Txn tx) {
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

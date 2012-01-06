package org.multiverse.api.collections;

import org.multiverse.api.Stm;
import org.multiverse.api.Transaction;
import org.multiverse.api.functions.BinaryFunction;
import org.multiverse.api.functions.Function;
import org.multiverse.api.predicates.Predicate;

import java.util.Collection;

/**
 *
 * @param <E>
 * @author Peter Veentjer.
 */
public interface TransactionalCollection<E> extends TransactionalIterable<E>, Collection<E> {

    /**
     * Returns the STM that manages this TransactionalCollection. Returned value will never be null.
     *
     * @return the STM that manages this TransactionalCollection.
     */
    Stm getStm();

    /**
     * {@inheritDoc}
     *
     * @return
     */
    boolean isEmpty();

    /**
     * Returns <tt>true</tt> if this collection contains no elements.
     *
     * @param tx the transaction used for this operation.
     * @return <tt>true</tt> if this collection contains no elements
     */
    boolean isEmpty(Transaction tx);

    int size();

    /**
     * Returns the number of elements in this collection.  If this collection
     * contains more than <tt>Integer.MAX_VALUE</tt> elements, returns
     * <tt>Integer.MAX_VALUE</tt>.
     *
     * @param tx the transaction used for this operation.
     * @return the number of elements in this collection
     */
    int size(Transaction tx);

    boolean contains(Object o);

    /**
     * Returns <tt>true</tt> if this collection contains the specified element.
     * More formally, returns <tt>true</tt> if and only if this collection
     * contains at least one element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>.
     *
     * @param tx the transaction used for this operation.
     * @param o  element whose presence in this collection is to be tested
     * @return <tt>true</tt> if this collection contains the specified
     *         element
     * @throws ClassCastException   if the type of the specified element
     *                              is incompatible with this collection (optional)
     * @throws NullPointerException if the specified element is null and this
     *                              collection does not permit null elements (optional)
     */
    boolean contains(Transaction tx, Object o);

    /**
     * Returns <tt>true</tt> if this collection contains all of the elements
     * in the specified collection.
     *
     * @param tx the transaction used for this operation.
     * @param c  collection to be checked for containment in this collection
     * @return <tt>true</tt> if this collection contains all of the elements
     *         in the specified collection
     * @throws ClassCastException   if the types of one or more elements
     *                              in the specified collection are incompatible with this
     *                              collection (optional)
     * @throws NullPointerException if the specified collection contains one
     *                              or more null elements and this collection does not permit null
     *                              elements (optional), or if the specified collection is null
     * @see #contains(Object)
     */
    boolean containsAll(Transaction tx, Collection<?> c);

    boolean remove(Object o);

    /**
     * Removes a single instance of the specified element from this
     * collection, if it is present (optional operation).  More formally,
     * removes an element <tt>e</tt> such that
     * <tt>(o==null&nbsp;?&nbsp;e==null&nbsp;:&nbsp;o.equals(e))</tt>, if
     * this collection contains one or more such elements.  Returns
     * <tt>true</tt> if this collection contained the specified element (or
     * equivalently, if this collection changed as a result of the call).
     *
     * @param tx the transaction used for this operation.
     * @param o element to be removed from this collection, if present
     * @return <tt>true</tt> if an element was removed as a result of this call
     * @throws ClassCastException            if the type of the specified element
     *                                       is incompatible with this collection (optional)
     * @throws NullPointerException          if the specified element is null and this
     *                                       collection does not permit null elements (optional)
     * @throws UnsupportedOperationException if the <tt>remove</tt> operation
     *                                       is not supported by this collection
     */
    boolean remove(Transaction tx, Object o);

    void clear();

    /**
     * Removes all of the elements from this collection (optional operation).
     * The collection will be empty after this method returns.
     *
     * @param tx the transaction used for this operation.
     * @throws UnsupportedOperationException if the <tt>clear</tt> operation
     *                                       is not supported by this collection
     */
    void clear(Transaction tx);

    boolean add(E item);

    /**
     * Ensures that this collection contains the specified element (optional
     * operation).  Returns <tt>true</tt> if this collection changed as a
     * result of the call.  (Returns <tt>false</tt> if this collection does
     * not permit duplicates and already contains the specified element.)<p>
     * <p/>
     * Collections that support this operation may place limitations on what
     * elements may be added to this collection.  In particular, some
     * collections will refuse to add <tt>null</tt> elements, and others will
     * impose restrictions on the type of elements that may be added.
     * Collection classes should clearly specify in their documentation any
     * restrictions on what elements may be added.<p>
     * <p/>
     * If a collection refuses to add a particular element for any reason
     * other than that it already contains the element, it <i>must</i> throw
     * an exception (rather than returning <tt>false</tt>).  This preserves
     * the invariant that a collection always contains the specified element
     * after this call returns.
     *
     * @param tx the transaction used for this operation.
     * @param e  element whose presence in this collection is to be ensured
     * @return <tt>true</tt> if this collection changed as a result of the
     *         call
     * @throws UnsupportedOperationException if the <tt>add</tt> operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the class of the specified element
     *                                       prevents it from being added to this collection
     * @throws NullPointerException          if the specified element is null and this
     *                                       collection does not permit null elements
     * @throws IllegalArgumentException      if some property of the element
     *                                       prevents it from being added to this collection
     * @throws IllegalStateException         if the element cannot be added at this
     *                                       time due to insertion restrictions
     */
    boolean add(Transaction tx, E e);


    boolean addAll(Collection<? extends E> c);

    /**
     * Adds all of the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in progress.
     * (This implies that the behavior of this call is undefined if the
     * specified collection is this collection, and this collection is
     * nonempty.)
     *
     * @param tx the transaction used for this operation.
     * @param c collection containing elements to be added to this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the class of an element of the specified
     *                                       collection prevents it from being added to this collection
     * @throws NullPointerException          if the specified collection contains a
     *                                       null element and this collection does not permit null elements,
     *                                       or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this
     *                                       collection
     * @throws IllegalStateException         if not all the elements can be added at
     *                                       this time due to insertion restrictions
     * @see #add(Object)
     */
    boolean addAll(Transaction tx, Collection<? extends E> c);

    /**
     * Adds all of the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in progress.
     * (This implies that the behavior of this call is undefined if the
     * specified collection is this collection, and this collection is
     * nonempty.)
     *
     * @param c collection containing elements to be added to this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the class of an element of the specified
     *                                       collection prevents it from being added to this collection
     * @throws NullPointerException          if the specified collection contains a
     *                                       null element and this collection does not permit null elements,
     *                                       or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this
     *                                       collection
     * @throws IllegalStateException         if not all the elements can be added at
     *                                       this time due to insertion restrictions
     * @see #add(Object)
     */
    boolean addAll(TransactionalCollection<? extends E> c);

    /**
     * Adds all of the elements in the specified collection to this collection
     * (optional operation).  The behavior of this operation is undefined if
     * the specified collection is modified while the operation is in progress.
     * (This implies that the behavior of this call is undefined if the
     * specified collection is this collection, and this collection is
     * nonempty.)
     *
     * @param c collection containing elements to be added to this collection
     * @return <tt>true</tt> if this collection changed as a result of the call
     * @throws UnsupportedOperationException if the <tt>addAll</tt> operation
     *                                       is not supported by this collection
     * @throws ClassCastException            if the class of an element of the specified
     *                                       collection prevents it from being added to this collection
     * @throws NullPointerException          if the specified collection contains a
     *                                       null element and this collection does not permit null elements,
     *                                       or if the specified collection is null
     * @throws IllegalArgumentException      if some property of an element of the
     *                                       specified collection prevents it from being added to this
     *                                       collection
     * @throws IllegalStateException         if not all the elements can be added at
     *                                       this time due to insertion restrictions
     * @see #add(Object)
     */
    boolean addAll(Transaction tx, TransactionalCollection<? extends E> c);

    TransactionalCollection<E> drop(int numToDrop);

    TransactionalCollection<E> drop(Transaction tx, int numToDrop);

    TransactionalCollection<E> dropWhile(Predicate<E> predicate);

    TransactionalCollection<E> dropWhile(Transaction tx, Predicate<E> predicate);

    String toString(Transaction tx);

    E foldLeft(BinaryFunction<E> function, E initial);

    E foldLeft(Transaction tx, BinaryFunction<E> function, E initial);

    E foldRight(BinaryFunction<E> function, E initial);

    E foldRight(Transaction tx, BinaryFunction<E> function, E initial);

    TransactionalCollection<E> map(Function<E> function);

    TransactionalCollection<E> map(Transaction tx, Function<E> function);

    TransactionalCollection<E> flatMap(Function<E> function);

    TransactionalCollection<E> flatMap(Transaction tx, Function<E> function);

    TransactionalCollection<E> filter(Predicate<E> predicate);

    TransactionalCollection<E> filter(Transaction tx, Predicate<E> predicate) throws CloneNotSupportedException;

    void foreach(Function<E> function);

    void foreach(Transaction tx, Function<E> function);

    TransactionalCollection<E> buildNew();
}

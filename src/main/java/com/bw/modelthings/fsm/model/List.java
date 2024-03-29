package com.bw.modelthings.fsm.model;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * <p>Implementation of the data-structures and algorithms described in the W3C scxml proposal.<br>
 * As reference each type and method has the w3c description as documentation.<br>
 * See <a href="https:/**www.w3.org/TR/scxml/#AlgorithmforSCXMLInterpretation">AlgorithmforSCXMLInterpretation</a>
 * </p>
 * <p>Structs and methods are designed to match the signatures in the W3c-Pseudo-code.</p>
 * <p>Additional it implements {@link Collection}</p>
 */
public class List<T> extends AbstractCollection<T>
{
	final LinkedList<T> _list = new LinkedList<>();

	/**
	 * Creates a new empty list.
	 */
	public List()
	{
	}

	@Override
	public Iterator<T> iterator()
	{
		return _list.iterator();
	}

	@Override
	public int size()
	{
		return _list.size();
	}

	/**
	 * Adds an element to the list.
	 *
	 * @param l The element to add.
	 */
	public boolean add(T l)
	{
		return _list.add(l);
	}

	/**
	 * Adds all elements to the list.
	 *
	 * @param l The list to add.
	 */
	public void add(Collection<T> l)
	{
		_list.addAll(l);
	}


	/**
	 * Creates a list filled with all elements from l.
	 *
	 * @param l The original list to copy.
	 */
	public List(List<? extends T> l)
	{
		_list.addAll(l._list);
	}

	/**
	 * Returns the head of the list.
	 *
	 * @return The first element.
	 * @throws java.util.NoSuchElementException if list is empty.
	 */
	public T head()
	{
		return _list.getFirst();
	}

	/**
	 * Returns the tail of the list (i.e., the rest of the list once the head is removed)
	 *
	 * @return A clone of the list without the first element.
	 */
	public List<T> tail()
	{
		List<T> t = new List<>(this);
		t._list.removeFirst();
		return t;
	}

	/**
	 * Returns the list appended with l
	 *
	 * @param l The Element to append.
	 * @return A clone of the list with the additional element.
	 */
	public List<T> append(T l)
	{
		List<T> t = new List<>(this);
		t._list.add(l);
		return t;
	}

	/**
	 * Returns the list appended with l
	 *
	 * @param l The list to append.
	 * @return A clone of the list with all elements from l appended at the end.
	 */
	public List<T> append(List<? extends T> l)
	{
		List<T> t = new List<>(this);
		t._list.addAll(l._list);
		return t;
	}

	/**
	 * Returns the list appended with l
	 *
	 * @param l The set to append.
	 * @return A clone of the list with all elements from l appended at the end.
	 */
	public List<T> append(OrderedSet<? extends T> l)
	{
		List<T> t = new List<>(this);
		t._list.addAll(l._set);
		return t;
	}


	/**
	 * Returns the list of elements that satisfy the predicate f
	 *
	 * @param f The predicate to filter for.
	 * @return A list with all matching elements.
	 */
	public List<T> filter(Predicate<T> f)
	{
		List<T> t = new List<>();
		_list.stream()
			 .filter(f)
			 .forEach(t._list::add);
		return t;
	}

	/**
	 * Returns true if some element in the list satisfies the predicate f.  Returns false for an empty list.
	 *
	 * @param f The predicate to search for.
	 * @return the result.
	 */
	public boolean some(Predicate<T> f)
	{
		return _list.stream()
					.anyMatch(f);
	}

	/**
	 * Returns true if every element in the list satisfies the predicate f.  Returns true for an e.
	 *
	 * @param f The predicate to search for.
	 * @return the result.
	 */
	public boolean every(Predicate<T> f)
	{
		return _list.stream()
					.allMatch(f);
	}

	/**
	 * Removes all elements.
	 */
	public void clear()
	{
		_list.clear();
	}
}
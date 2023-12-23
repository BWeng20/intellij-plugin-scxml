package com.bw.modeldrive.model;

import java.util.LinkedList;
import java.util.function.Predicate;

/**
 * <p>Implementation of the data-structures and algorithms described in the W3C scxml proposal.<br>
 * As reference each type and method has the w3c description as documentation.<br>
 * See <a href="https:/**www.w3.org/TR/scxml/#AlgorithmforSCXMLInterpretation">AlgorithmforSCXMLInterpretation</a>
 * </p>
 * <p>Structs and methods are designed to match the signatures in the W3c-Pseudo-code.</p>
 */
public class List<T>
{
	final LinkedList<T> _list = new LinkedList<>();

	public List()
	{
	}

	public List(List<? extends T> list)
	{
		_list.addAll(list._list);
	}


	/**
	 * Returns the head of the list
	 */
	public T head()
	{
		return _list.getFirst();
	}

	/**
	 * Returns the tail of the list (i.e., the rest of the list once the head is removed)
	 */
	public List<T> tail()
	{
		List<T> t = new List<>(this);
		t._list.removeFirst();
		return t;
	}

	/**
	 * Returns the list appended with l
	 */
	public List<T> append(T e)
	{
		List<T> t = new List<>(this);
		t._list.add(e);
		return t;
	}

	/**
	 * Returns the list appended with l
	 */
	public List<T> append(List<? extends T> e)
	{
		List<T> t = new List<>(this);
		t._list.addAll(e._list);
		return t;
	}

	/**
	 * Returns the list appended with l
	 */
	public List<T> append(OrderedSet<? extends T> e)
	{
		List<T> t = new List<>(this);
		t._list.addAll(e._set);
		return t;
	}


	/**
	 * Returns the list of elements that satisfy the predicate f
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
	 */
	public boolean some(Predicate<T> f)
	{
		return _list.stream()
					.anyMatch(f);
	}

	/**
	 * Returns true if every element in the list satisfies the predicate f.  Returns true for an e.
	 */
	public boolean every(Predicate<T> f)
	{
		return _list.stream()
					.allMatch(f);
	}
}
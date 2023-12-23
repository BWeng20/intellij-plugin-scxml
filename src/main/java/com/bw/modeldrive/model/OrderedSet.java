package com.bw.modeldrive.model;

import com.twelvemonkeys.util.LinkedSet;

import java.util.function.Predicate;

/**
 * <p>Implementation of the data-structures and algorithms described in the W3C scxml proposal.<br>
 * As reference each type and method has the w3c description as documentation.<br>
 * See <a href="https://www.w3.org/TR/scxml/#AlgorithmforSCXMLInterpretation">AlgorithmforSCXMLInterpretation</a>
 * </p>
 * <p>Structs and methods are designed to match the signatures in the W3c-Pseudo-code.</p>
 */
public class OrderedSet<T>
{
	LinkedSet<T> _set = new LinkedSet<>();

	/**
	 * Adds e to the set if it is not already a member
	 */
	public void add(T e)
	{
		_set.add(e);
	}

	/**
	 * Deletes e from the set
	 */
	public void delete(T e)
	{
		_set.remove(e);
	}

	/**
	 * Adds all members of s that are not already members of the set (s must also be an OrderedSet)
	 */
	public void union(OrderedSet<T> s)
	{
		s._set.addAll(s._set);
	}

	/**
	 * Is e a member of set?
	 */
	public boolean isMember(T e)
	{
		return _set.contains(e);
	}

	/**
	 * Returns true if some element in the set satisfies the predicate f.  Returns false for an empty set.
	 */
	public boolean some(Predicate<T> f)
	{
		return _set.stream()
				   .anyMatch(f);
	}

	/**
	 * Returns true if every element in the set satisfies the predicate f. Returns true for an empty set.
	 */
	public boolean every(Predicate<T> f)
	{
		return _set.stream()
				   .allMatch(f);
	}

	/**
	 * Returns true if this set and  set s have at least one member in common
	 */
	public boolean hasIntersection(OrderedSet<? extends T> s)
	{
		return _set.stream()
				   .anyMatch(t -> s._set.contains(t));
	}

	/**
	 * Is the set empty?
	 */
	public boolean isEmpty()
	{
		return _set.isEmpty();
	}

	/**
	 * Remove all elements from the set (make it empty)
	 */
	public void clear()
	{
		_set.clear();
	}

	/**
	 * Converts the set to a list that reflects the order in which elements were originally added<br>
	 * In the case of sets created by intersection, the order of the first set (the one on which the method was called) is used<br>
	 * In the case of sets created by union, the members of the first set (the one on which union was called) retain their original ordering
	 * while any members belonging to the second set only are placed after, retaining their ordering in their original set.
	 */
	public List<T> toList()
	{
		List<T> list = new List<>();
		list._list.addAll(_set);
		return list;
	}

}

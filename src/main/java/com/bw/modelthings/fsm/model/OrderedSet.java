package com.bw.modelthings.fsm.model;

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
	 * Created a new empty set.
	 */
	OrderedSet()
	{
	}


	/**
	 * Adds e to the set if it is not already a member
	 *
	 * @param e The element to add.
	 */
	public void add(T e)
	{
		_set.add(e);
	}

	/**
	 * Deletes e from the set
	 *
	 * @param e The element to remove.
	 */
	public void delete(T e)
	{
		_set.remove(e);
	}

	/**
	 * Adds all members of s that are not already members of the set (s must also be an OrderedSet)
	 *
	 * @param s The set to add.
	 */
	public void union(OrderedSet<T> s)
	{
		s._set.addAll(s._set);
	}

	/**
	 * Is e a member of set?
	 *
	 * @param e The element to search for.
	 * @return true if e is in the set.
	 */
	public boolean isMember(T e)
	{
		return _set.contains(e);
	}

	/**
	 * Checks for predicate.
	 *
	 * @param f The predicate to check.
	 * @return true if some element in the set satisfies the predicate f.  Returns false for an empty set.
	 */
	public boolean some(Predicate<T> f)
	{
		return _set.stream()
				   .anyMatch(f);
	}

	/**
	 * Checks for predicate.
	 *
	 * @param f The predicate to check.
	 * @return true if every element in the set satisfies the predicate f. Returns true for an empty set.
	 */
	public boolean every(Predicate<T> f)
	{
		return _set.stream()
				   .allMatch(f);
	}

	/**
	 * Checks for intersection.
	 *
	 * @param s The set to check.
	 * @return true if this set and set s have at least one member in common
	 */
	public boolean hasIntersection(OrderedSet<? extends T> s)
	{
		return _set.stream()
				   .anyMatch(t -> s._set.contains(t));
	}

	/**
	 * Is the set empty?
	 *
	 * @return true if empty
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
	 *
	 * @return the created list.
	 */
	public List<T> toList()
	{
		List<T> list = new List<>();
		list._list.addAll(_set);
		return list;
	}

}

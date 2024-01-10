package com.bw.modeldrive.fsm.model;

/**
 * <p>Implementation of the data-structures and algorithms described in the W3C scxml proposal.<br>
 * As reference each type and method has the w3c description as documentation.<br>
 * See <a href="https://www.w3.org/TR/scxml/#AlgorithmforSCXMLInterpretation">AlgorithmforSCXMLInterpretation</a>
 * </p>
 * <p>Structs and methods are designed to match the signatures in the W3c-Pseudo-code.</p>
 */
public class Queue<T>
{
	java.util.Deque<T> _queue = new java.util.LinkedList<>();

	/**
	 * Creates a new empty queue.
	 */
	public Queue()
	{

	}

	/**
	 * Puts e last in the queue.
	 *
	 * @param e The element to add.
	 */
	public void enqueue(T e)
	{
		_queue.addLast(e);
	}

	/**
	 * Removes and returns first element in queue.
	 *
	 * @return The first element or null if empty.
	 */
	public T dequeue()
	{
		return _queue.pollFirst();
	}

	/**
	 * Is the queue empty?
	 *
	 * @return true if empty.
	 */
	public boolean isEmpty()
	{
		return _queue.isEmpty();
	}
}

package com.bw.modelthings.fsm.model;

/**
 * <p>Implementation of the data-structures and algorithms described in the W3C scxml proposal.<br>
 * As reference each type and method has the w3c description as documentation.<br>
 * See <a href="https://www.w3.org/TR/scxml/#AlgorithmforSCXMLInterpretation">Algorithm for SCXMLInterpretation</a>
 * </p>
 * <p>Structs and methods are designed to match the signatures in the W3c-Pseudo-code.</p>
 */
public class BlockingQueue<T>
{
	java.util.concurrent.LinkedBlockingQueue<T> _queue;
	boolean _stopped = false;

	/**
	 * Creates a new BlockingQueue.
	 */
	public BlockingQueue()
	{
	}

	/**
	 * Puts e last in the queue
	 *
	 * @param e The element to enqueue. Must not be null.
	 */
	public void enqueue(T e)
	{
		_queue.add(e);
	}

	/**
	 * Removes and returns first element in queue, blocks if queue is empty.
	 *
	 * @return The element, null only if machine stops.
	 */
	public T dequeue()
	{
		do
		{
			try
			{
				T e = _queue.take();
				if (!_stopped)
					return e;
			}
			catch (InterruptedException e)
			{
			}
		} while (!_stopped);
		return null;
	}
}

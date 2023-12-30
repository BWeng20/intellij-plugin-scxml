package com.bw.modeldrive.model;

/**
 * <p><b>W3C says:</b><br>
 * <em>Global variables</em><br>
 * The following variables are global from the point of view of the algorithm.
 * Their values will be set in the procedure interpret().<br></p>
 * <p><b>Actual Implementation</b><br>
 * In the W3C algorithm the data is available via global variables.
 * In java there is not global scope, so the data is stored in this class.</p>
 */
public class GlobalData
{
	/**
	 * The list of currently active states.
	 */
	public OrderedSet<StateId> configuration;

	/**
	 * The list of states to invoke on next iteration.
	 */
	public OrderedSet<StateId> statesToInvoke;

	/**
	 * The history for each state.
	 */
	public HashTable<StateId, OrderedSet<StateId>> historyValue;

	/**
	 * True if running.
	 */
	public boolean running;

	/**
	 * The queue of internal events. All internal events are processed before the next external is handled.
	 */
	public Queue<Event> internalQueue;

	/**
	 * The queue of external events.
	 */
	public BlockingQueue<Event> externalQueue;

}

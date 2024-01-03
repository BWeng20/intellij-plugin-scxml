package com.bw.modeldrive.model;

import java.util.ArrayList;

/**
 * Holds the representation of a state.
 */
public class State
{

	/**
	 * Creates a new state.
	 */
	public State()
	{

	}

	/**
	 * The unique id, counting in document order.<br>
	 * "id" is increasing on references to states, not declaration and may not result in correct order.
	 */
	public int docId;

	/**
	 * The SCXML id.
	 */
	public String name;

	/**
	 * The initial transition id (if the state has sub-states).
	 */
	public Transition initial;

	/**
	 * The sub-states of this state.
	 */
	public final java.util.List<State> states = new ArrayList<>();

	/**
	 * True for "parallel" states
	 */
	public boolean isParallel = false;

	/**
	 * True for "final" states
	 */
	public boolean isFinal = false;

	/**
	 * The type of hosztory for this state.
	 */
	public HistoryType historyType;

	/**
	 * The script that is executed if the state is entered. See W3c comments for &lt;onentry&gt; above.
	 */
	public ExecutableContent onentry;

	/**
	 * The script that is executed if the state is left. See W3c comments for &lt;onexit&gt; above.
	 */
	public ExecutableContent onexit;

	/**
	 * All transitions between sub-states.
	 */
	public List<Transition> transitions = new List<>();

	/**
	 * List of invokes to execute if state is entered.
	 */
	public java.util.List<Invoke> invoke;

	/**
	 * State history.
	 */
	public java.util.List<State> history;

	/**
	 * The local datamodel
	 */
	public DataStore data;

	/**
	 * True if state was never entered.
	 */
	public boolean isFirstEntry;

	/**
	 * The parent state or null.
	 */
	public State parent;

	/**
	 * DoneData of final states or null.
	 */
	public DoneData donedata;

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(50);
		sb.append(name);
		if (isParallel)
			sb.append(" (parallel)");
		if (isFinal)
			sb.append(" (final)");
		return sb.toString();
	}

}

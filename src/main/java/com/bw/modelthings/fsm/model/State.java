package com.bw.modelthings.fsm.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Holds the representation of a state.
 */
public class State implements FsmElement
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
	public int _docId;

	/**
	 * The SCXML id.
	 */
	public String _name;

	/**
	 * The initial transition id (if the state has sub-states).
	 */
	public Transition _initial;

	/**
	 * The sub-states of this state.
	 */
	public final java.util.List<State> _states = new ArrayList<>();

	/**
	 * True for "parallel" states
	 */
	public boolean _isParallel = false;

	/**
	 * True for "final" states
	 */
	public boolean _isFinal = false;

	/**
	 * The type of hosztory for this state.
	 */
	public HistoryType _historyType;

	/**
	 * The script that is executed if the state is entered. See W3c comments for &lt;onentry&gt; above.
	 */
	public ExecutableContent _onEntry;

	/**
	 * The script that is executed if the state is left. See W3c comments for &lt;onexit&gt; above.
	 */
	public ExecutableContent _onExit;

	/**
	 * All transitions between sub-states.
	 */
	public List<Transition> _transitions = new List<>();

	/**
	 * List of invokes to execute if state is entered.
	 */
	public java.util.List<Invoke> _invoke;

	/**
	 * State history.
	 */
	public java.util.List<State> _history;

	/**
	 * The local datamodel
	 */
	public DataStore _data;

	/**
	 * True if state was never entered.
	 */
	public boolean _isFirstEntry;

	/**
	 * The parent state or null.
	 */
	public State _parent;

	/**
	 * DoneData of final states or null.
	 */
	public DoneData _doneData;

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(50);
		sb.append(_name);
		if (_isParallel)
			sb.append(" (parallel)");
		if (_isFinal)
			sb.append(" (final)");
		return sb.toString();
	}

	/**
	 * Get all substates in document declaration order.
	 *
	 * @return The list of states. Never null.
	 */
	public java.util.List<State> getInnerStatesInDocumentOrder()
	{
		State[] statesArray = new State[_states.size()];
		statesArray = _states.toArray(statesArray);
		Arrays.sort(statesArray, Comparator.comparingInt(s -> s._docId));
		return Arrays.asList(statesArray);
	}

}

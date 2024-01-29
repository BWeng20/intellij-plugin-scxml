package com.bw.modelthings.fsm.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

/**
 * The implementation of the FSM.
 */
public class FiniteStateMachine
{
	/**
	 * Creates a new empty State Machine.
	 */
	public FiniteStateMachine()
	{
	}

	/**
	 * The trace, possibly null.
	 */
	public Tracer _tracer;

	/**
	 * The data model type to use.
	 */
	public String _dataModel;

	/**
	 * The binding mode
	 */
	public BindingType _binding;

	/**
	 * The version
	 */
	public String _version;

	/**
	 * All states by Id
	 */
	public final HashMap<String, State> _states = new HashMap<>();

	/**
	 * The name of the FSM.
	 */
	public String _name;

	/**
	 * An FSM can have actual multiple initial-target-states, so this state may be artificial.
	 * Reader has to generate a parent state if needed.
	 * This state also serve as the "scxml" state element were mentioned.
	 */
	public State _pseudoRoot;

	/**
	 * <strong>W3C says:</strong><br>
	 * The &lt;script&gt; element adds scripting capability to the state machine.
	 */
	public ExecutableContent _script;

	/**
	 * The invoke-id of the caller or null.
	 */
	public String _callerInvokeId;

	/**
	 * Timer used for delays.
	 */
	public Timer _timer;

	/**
	 * Removes a state.
	 *
	 * @param state         The state to remove.
	 * @param keepSubStates If true child-states and internal transitions are moved to parent.
	 * @return The list of actual removes states.
	 */
	public List<State> remove(State state, boolean keepSubStates)
	{
		List<State> removed = new ArrayList<>();
		removeState(_pseudoRoot, state, keepSubStates, removed);
		return removed;
	}

	/**
	 * Removes a state with connected transitions and child-states.
	 *
	 * @param startAt       State to start at with clean-up.
	 * @param stateToRemove The state to remove.
	 * @param keepSubStates If true, child-states are moved to parent.
	 * @param removed       Collects all removed state.
	 */
	public void removeState(State startAt, State stateToRemove, boolean keepSubStates, List<State> removed)
	{
		if (stateToRemove != null)
		{
			if (stateToRemove._parent != null)
			{
				// Walk down the tree of the state to move states and transitions recursively up.
				List<State> tmp= new ArrayList<>(stateToRemove._states);
				for (State s : tmp)
					removeState(null, s, keepSubStates, removed);

				if (keepSubStates)
				{
					// Keep states.
					stateToRemove._parent._states.addAll(stateToRemove._states);
					// Keep also transitions.
					stateToRemove._parent._transitions.addAll(stateToRemove._transitions);
				}

				stateToRemove._parent._states.remove(stateToRemove);
				stateToRemove._parent = null;

				// Children should be empty already, no need to stateToRemove.states.clear();
				stateToRemove._transitions.clear();
				if (stateToRemove._history != null)
					stateToRemove._history.clear();
			}
			if (startAt != null || !keepSubStates)
				removed.add(stateToRemove);
		}
		// At this point "removed" should contain already all states that are removed.

		if (startAt != null && !removed.isEmpty())
		{
			// We are in the top most call or already going down startAt-tree.

			Iterator<Transition> tit = startAt._transitions.iterator();
			while (tit.hasNext())
			{
				Transition t = tit.next();
				if (removed.contains(t._source) || (t._target.removeAll(removed) && t._target.isEmpty()))
					tit.remove();
			}
			if (startAt._history != null)
				startAt._history.removeAll(removed);
			for (State child : startAt._states)
			{
				removeState(child, null, false, removed);
			}
		}
	}


	/**
	 * Clear resources
	 */
	public void dispose()
	{
		// @TODO: anything?
	}
}

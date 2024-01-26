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
	public Tracer tracer;

	/**
	 * The data model type to use.
	 */
	public String datamodel;

	/**
	 * The binding mode
	 */
	public BindingType binding;

	/**
	 * The version
	 */
	public String version;

	/**
	 * All states by Id
	 */
	public final HashMap<String, State> states = new HashMap<>();

	/**
	 * The name of the FSM.
	 */
	public String name;

	/**
	 * An FSM can have actual multiple initial-target-states, so this state may be artificial.
	 * Reader has to generate a parent state if needed.
	 * This state also serve as the "scxml" state element were mentioned.
	 */
	public State pseudoRoot;

	/**
	 * <strong>W3C says:</strong><br>
	 * The &lt;script&gt; element adds scripting capability to the state machine.
	 */
	public ExecutableContent script;

	/**
	 * The invoke-id of the caller or null.
	 */
	public String callerInvokeId;

	/**
	 * Timer used for delays.
	 */
	public Timer timer;

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
		removeState(pseudoRoot, state, keepSubStates, removed);
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
			if (stateToRemove.parent != null)
			{
				// Walk down the tree of the state to remove and move states and transitions recursively up.
				for (State s : stateToRemove.states)
					removeState(null, s, keepSubStates, removed);

				if (keepSubStates)
				{
					// Keep states.
					stateToRemove.parent.states.addAll(stateToRemove.states);
					// Keep also transitions.
					stateToRemove.parent.transitions.addAll(stateToRemove.transitions);
				}
				stateToRemove.parent.states.remove(stateToRemove);
				stateToRemove.parent = null;

				// Children should be empty already, no need to stateToRemove.states.clear();
				stateToRemove.transitions.clear();
				if (stateToRemove.history != null)
					stateToRemove.history.clear();
				removed.add(stateToRemove);
			}
		}
		// At this point "removed" should contain already all states that are removed.

		if (startAt != null && !removed.isEmpty())
		{
			// We are in the top most call or already going down startAt-tree.

			Iterator<Transition> tit = startAt.transitions.iterator();
			while (tit.hasNext())
			{
				Transition t = tit.next();
				if (removed.contains(t.source) || (t.target.removeAll(removed) && t.target.isEmpty()))
					tit.remove();
			}
			if (startAt.history != null)
				startAt.history.removeAll(removed);
			for (State child : startAt.states)
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

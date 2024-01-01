package com.bw.modeldrive.model;

import java.util.ArrayList;
import java.util.HashMap;
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
	 * Possible outgoing transitions from this state.
	 */
	public final java.util.List<Transition> transitions = new ArrayList<>();

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
}

package com.bw.modeldrive.model;

import java.util.HashMap;
import java.util.Timer;

/**
 * The implementation of the FSM.
 */
public class FinitStateMachine
{

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
	 * Executable content
	 */
	public final HashMap<ExecutableContentId, List<ExecutableContent>> executableContent = new HashMap<>();

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

	public final HashMap<TransitionId, Transition> transitions = new HashMap<>();

	public ExecutableContent script;

	public InvokeId callerInvokeId;

	public Timer timer;
}

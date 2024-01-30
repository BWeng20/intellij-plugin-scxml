package com.bw.modelthings.fsm.model;

/**
 * Holds the representation of the pseudo root state.
 */
public class PseudoRoot extends State
{
	/**
	 * Creates a new root state.
	 *
	 * @param fsm The FSM of the root state.
	 */
	public PseudoRoot(FiniteStateMachine fsm)
	{
		_fsm = fsm;
		_fsmName = fsm._name;
	}

	/**
	 * The state property {@link #_name} of the pseudo root has a synthetic value,
	 * as attribute "name" from &lt;scxml&gt; is not guaranteed to be unique.<br>
	 * Instead, "fsmName" stores the attribute "name".
	 */
	public String _fsmName;

	/**
	 * The FSM.
	 */
	public final FiniteStateMachine _fsm;

}

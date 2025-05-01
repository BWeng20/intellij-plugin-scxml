package com.bw.modelthings.fsm.model;

/**
 * Some executable content.
 */
public abstract class ExecutableContent extends FsmElement
{
	/**
	 * Executes
	 *
	 * @param datamodel The datamodel to work on.
	 * @param fsm       The state machine to operate.
	 */
	public abstract void execute(Datamodel datamodel, FiniteStateMachine fsm);

	/**
	 * Get the type name of this content.
	 *
	 * @return the type of this content.
	 */
	public abstract String getType();

}

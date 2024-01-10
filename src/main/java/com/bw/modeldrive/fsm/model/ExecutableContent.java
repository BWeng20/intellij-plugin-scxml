package com.bw.modeldrive.fsm.model;

/**
 * Some executable content.
 */
public interface ExecutableContent
{
	/**
	 * Executes
	 *
	 * @param datamodel The datamodel to work on.
	 * @param fsm       The state machine to operate.
	 */
	void execute(Datamodel datamodel, FiniteStateMachine fsm);

	/**
	 * Get the type name of this content.
	 *
	 * @return the type of this content.
	 */
	String getType();

}

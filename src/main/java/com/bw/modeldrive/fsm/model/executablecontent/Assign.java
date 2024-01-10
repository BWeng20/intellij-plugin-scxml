package com.bw.modeldrive.fsm.model.executablecontent;

import com.bw.modeldrive.fsm.model.Datamodel;
import com.bw.modeldrive.fsm.model.ExecutableContent;
import com.bw.modeldrive.fsm.model.FiniteStateMachine;

/**
 * Assignment.
 */
public class Assign implements ExecutableContent
{
	/**
	 * Creates a new empty assign operation.
	 */
	public Assign()
	{
	}

	@Override
	public void execute(Datamodel datamodel, FiniteStateMachine fsm)
	{
		// @TODO
	}

	@Override
	public String getType()
	{
		return "Assign";
	}
}

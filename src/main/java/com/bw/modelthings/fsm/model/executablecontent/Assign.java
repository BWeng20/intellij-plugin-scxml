package com.bw.modelthings.fsm.model.executablecontent;

import com.bw.modelthings.fsm.model.Datamodel;
import com.bw.modelthings.fsm.model.ExecutableContent;
import com.bw.modelthings.fsm.model.FiniteStateMachine;

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

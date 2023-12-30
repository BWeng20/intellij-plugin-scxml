package com.bw.modeldrive.model.executablecontent;

import com.bw.modeldrive.model.Datamodel;
import com.bw.modeldrive.model.ExecutableContent;
import com.bw.modeldrive.model.FiniteStateMachine;

/**
 * Assignment.
 */
public class Assign implements ExecutableContent
{
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

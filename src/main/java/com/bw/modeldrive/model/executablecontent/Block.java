package com.bw.modeldrive.model.executablecontent;

import com.bw.modeldrive.model.Datamodel;
import com.bw.modeldrive.model.ExecutableContent;
import com.bw.modeldrive.model.FiniteStateMachine;

import java.util.ArrayList;

/**
 * List of executable content.
 */
public class Block implements ExecutableContent
{
	/**
	 * The list of operations
	 */
	public final java.util.List<ExecutableContent> content = new ArrayList<>();

	/**
	 * Creates an empty block.
	 */
	public Block()
	{
	}

	@Override
	public void execute(Datamodel datamodel, FiniteStateMachine fsm)
	{
		for (ExecutableContent e : content)
		{
			e.execute(datamodel, fsm);
		}
	}

	@Override
	public String getType()
	{
		return "Block";
	}
}

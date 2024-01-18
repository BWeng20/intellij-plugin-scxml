package com.bw.modelthings.fsm.model.executablecontent;

import com.bw.modelthings.fsm.model.Datamodel;
import com.bw.modelthings.fsm.model.ExecutableContent;
import com.bw.modelthings.fsm.model.FiniteStateMachine;

/**
 * A container for conditionally executed elements.
 */
public class If implements ExecutableContent
{

	/**
	 * Creates a new "if" element.
	 *
	 * @param condition The Conditional expression.
	 */
	public If(String condition)
	{
		this.condition = condition;
	}

	/**
	 * The Conditional expression.
	 */
	public final String condition;

	/**
	 * The Content to execute if condition is true.
	 */
	public ExecutableContent content;

	/**
	 * The Content to execute if condition is false.
	 */
	public ExecutableContent elseContent;

	@Override
	public void execute(Datamodel datamodel, FiniteStateMachine fsm)
	{
		if (datamodel.executeCondition(fsm, this.condition))
		{
			if (content != null)
				content.execute(datamodel, fsm);
			;
		}
		else if (elseContent != null)
			elseContent.execute(datamodel, fsm);
	}

	@Override
	public String getType()
	{
		return "Block";
	}


}

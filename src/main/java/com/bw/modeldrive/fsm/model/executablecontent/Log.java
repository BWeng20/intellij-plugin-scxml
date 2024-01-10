package com.bw.modeldrive.fsm.model.executablecontent;

import com.bw.modeldrive.fsm.model.Datamodel;
import com.bw.modeldrive.fsm.model.ExecutableContent;
import com.bw.modeldrive.fsm.model.FiniteStateMachine;

/**
 * <strong>W3C says:</strong><br>
 * &lt;log&gt; allows an application to generate a logging or debug message.
 */
public class Log implements ExecutableContent
{

	/**
	 * Creates a nre log operation.
	 *
	 * @param label      The label can be null
	 * @param expression The expression to log, can be null (but makes no sense).
	 */
	public Log(String label, String expression)
	{
		this.label = label;
		this.expression = expression;
	}

	/**
	 * <strong>W3C says:</strong><br>
	 * A character string with an implementation-dependent interpretation. It is intended to provide meta-data about the log string specified by 'expr'.
	 */
	public String label;

	/**
	 * <strong>W3C says:</strong><br>
	 * An expression returning the value to be logged. See 5.9.3 Legal Data Values and Value Expressions for details.
	 * The nature of the logging mechanism is implementation-dependent. For example, the SCXML processor may convert this value
	 * to a convenient format before logging it.
	 */
	public String expression;

	@Override
	public void execute(Datamodel datamodel, FiniteStateMachine fsm)
	{
		datamodel.log(label, datamodel.execute(fsm, expression));
	}

	@Override
	public String getType()
	{
		return "log";
	}
}

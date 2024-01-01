package com.bw.modeldrive.model;

import java.util.ArrayList;
import java.util.List;

/**
 * <strong>W3C says:</strong><br>
 * Transitions between states are triggered by events and conditionalized via guard conditions.
 * They may contain executable content, which is executed when the transition is taken.
 * <br>
 * A conformant SCXML document must specify at least one of 'event', 'cond' or 'target'.
 * 3.13 Selecting and Executing Transitions contains more detail on the semantics of transitions.
 */
public class Transition
{

	/**
	 * Creates a new transition.
	 */
	public Transition()
	{

	}

	/**
	 * The unique id, counting in document order.<br>
	 */
	public int docId;

	/**
	 * <strong>W3C says:</strong><br>
	 * A list of designators of events that trigger this transition. See 3.13 Selecting and Executing Transitions for details on how transitions are selected and executed.
	 * See E Schema for the definition of the datatype.
	 */
	// @TODO: Possibly we need some type to express event ids
	public final List<String> events = new ArrayList<>();

	/**
	 * The guard condition for this transition. See 3.13 Selecting and Executing Transitions for details.
	 */
	public String cond;

	/**
	 * The source state.
	 */
	// @TODO: is this needed?
	public State source;

	/**
	 * <strong>W3C says:</strong><br>
	 * The state or parallel region to transition to. See 3.13 Selecting and Executing Transitions for details.
	 */
	public final java.util.List<State> target = new ArrayList<>();

	/**
	 * <strong>W3C says:</strong><br>
	 * Determines whether the source state is exited in transitions whose target state is a descendant of the source state.
	 * See 3.13 Selecting and Executing Transitions for details.
	 */
	public TransitionType transitionType;

	/**
	 * <strong>W3C says:</strong><br>
	 * The children of &lt;transition&gt; are executable content that is run after all the &lt;onexit&gt; handlers and before the all &lt;onentry&gt; handlers that
	 * are triggered by this transition. See 4 Executable Content
	 */
	public ExecutableContent content;

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(100);
		sb.append('[');
		if (source != null)
			sb.append(source.name);
		sb.append(']');
		if (cond != null)
			sb.append(" {")
			  .append(cond)
			  .append('}');
		if (transitionType != null)
			sb.append(" <")
			  .append(transitionType)
			  .append('>');

		sb.append(" -> ");
		boolean first = true;
		for (State t : target)
		{
			if (first)
				first = false;
			else
				sb.append(",");
			sb.append(t.name);
		}

		return sb.toString();
	}

}

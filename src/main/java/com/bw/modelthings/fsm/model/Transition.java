package com.bw.modelthings.fsm.model;

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
public class Transition implements FsmElement
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
	public int _docId;

	/**
	 * <strong>W3C says:</strong><br>
	 * A list of designators of events that trigger this transition. See 3.13 Selecting and Executing Transitions for details on how transitions are selected and executed.
	 * See E Schema for the definition of the datatype.
	 */
	// @TODO: Possibly we need some type to express event ids
	public final List<String> _events = new ArrayList<>();

	/**
	 * The guard condition for this transition. See 3.13 Selecting and Executing Transitions for details.
	 */
	public String _cond;

	/**
	 * The source state.
	 */
	// @TODO: is this needed?
	public State _source;

	/**
	 * <strong>W3C says:</strong><br>
	 * The state or parallel region to transition to. See 3.13 Selecting and Executing Transitions for details.
	 */
	public final java.util.List<State> _target = new ArrayList<>();

	/**
	 * <strong>W3C says:</strong><br>
	 * Determines whether the source state is exited in transitions whose target state is a descendant of the source state.
	 * See 3.13 Selecting and Executing Transitions for details.
	 */
	public TransitionType _transitionType;

	/**
	 * <strong>W3C says:</strong><br>
	 * The children of &lt;transition&gt; are executable content that is run after all the &lt;onexit&gt; handlers and before the all &lt;onentry&gt; handlers that
	 * are triggered by this transition. See 4 Executable Content
	 */
	public ExecutableContent _content;

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder(100);
		sb.append('[');
		if (_source != null)
			sb.append(_source._name);
		sb.append(']');
		if (_cond != null)
			sb.append(" {")
			  .append(_cond)
			  .append('}');
		if (_transitionType != null)
			sb.append(" <")
			  .append(_transitionType)
			  .append('>');

		sb.append(" -> ");
		boolean first = true;
		for (State t : _target)
		{
			if (first)
				first = false;
			else
				sb.append(",");
			sb.append(t._name);
		}

		return sb.toString();
	}

}

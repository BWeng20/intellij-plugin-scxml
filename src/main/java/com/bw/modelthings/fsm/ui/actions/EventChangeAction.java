package com.bw.modelthings.fsm.ui.actions;

import com.bw.graph.editor.action.EditAction;
import com.bw.modelthings.fsm.ui.TransitionVisual;

import java.util.List;

/**
 * Edit action to change a transition event list.
 */
public class EventChangeAction implements EditAction
{

	/**
	 * Initialize a new action.
	 *
	 * @param what   The visual that was changed.
	 * @param events The new list of events.
	 */
	public EventChangeAction(TransitionVisual what, List<String> events)
	{
		_what = what;
		_events = events;
	}


	/**
	 * The new list of trigger events.
	 */
	public final List<String> _events;

	/**
	 * The transition visual that was changed.
	 */
	public final TransitionVisual _what;

}

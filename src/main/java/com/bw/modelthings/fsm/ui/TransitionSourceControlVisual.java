package com.bw.modelthings.fsm.ui;

import com.bw.graph.DrawContext;
import com.bw.graph.primitive.Text;
import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.Visual;
import com.bw.graph.visual.VisualFlags;
import com.bw.modelthings.fsm.model.Transition;

/**
 * Visual to represent an FSM State Transition Source-Connector .
 */
public class TransitionSourceControlVisual extends ConnectorVisual
{
	/**
	 * Creates a new Transition Source Control Visual.
	 *
	 * @param sourceState The source visual.
	 * @param transition  The transition to represent with the visual.
	 * @param context     The draw context. Must not be null.
	 * @param flags       The initial flags. @see {@link VisualFlags}
	 */
	public TransitionSourceControlVisual(Visual sourceState, Transition transition, DrawContext context, int flags)
	{
		super(sourceState, context, flags);
		if (transition != null)
		{
			String events = String.join(",", transition._events );
			if (!events.isEmpty())
			{
				Text eventText = new Text(_radius + 5, _radius / 2, events,
						context._configuration, context._style, VisualFlags.ALWAYS);
				eventText.setFlags(VisualFlags.EDITABLE);
				// eventText.setUserData(_nameProxy);
				_primitives.add(eventText);
			}
		}
	}
}

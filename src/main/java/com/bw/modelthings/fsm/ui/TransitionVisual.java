package com.bw.modelthings.fsm.ui;

import com.bw.graph.DrawContext;
import com.bw.graph.util.Geometry;
import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.MultiTargetEdgeVisual;
import com.bw.graph.visual.Visual;
import com.bw.graph.visual.VisualFlags;
import com.bw.modelthings.fsm.model.Transition;

import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.AbstractMap;
import java.util.List;

/**
 * Visual to represent an FSM Transition.<br>
 * Transitions can have multiple targets, but only one source.
 */
public class TransitionVisual extends MultiTargetEdgeVisual
{
	private String _events;
	private String _condition;

	private boolean _drawContent;


	/**
	 * Creates a new Transition Visual.
	 *
	 * @param id          The identification (name) of the transition.
	 * @param sourceState The source visual.
	 * @param transition  The transition to represent with the visual.
	 * @param targets     The list of targets.
	 * @param context     The draw context. Must not be null.
	 * @param flags       The initial flags. @see {@link VisualFlags}
	 */
	public TransitionVisual(String id, Visual sourceState, Transition transition,
							List<AbstractMap.SimpleEntry<StateVisual, StateVisual>> targets,
							DrawContext context, int flags)
	{
		super(id, context);
		setFlags(flags);
		_source = new ConnectorVisual(sourceState, context, VisualFlags.ALWAYS);

		targets.forEach(targetPair -> {
			ConnectorVisual cv = new ConnectorVisual(targetPair.getKey(), context, VisualFlags.ALWAYS);
			cv.setTargetedParentChild(targetPair.getValue());
			addTarget(cv);
		});

		if (transition != null)
		{
			_events = String.join("/", transition._events);
			if (_events.isEmpty())
				_events = null;
			else
				_events = "[" + _events + "]";

			_condition = transition._cond;
			if (_condition == null || _condition.isEmpty())
				_condition = null;
			else
				_condition = "?" + _condition;

			_drawContent = (_events != null || _condition != null);
		}
	}

	/**
	 * Draws the transition.
	 *
	 * @param g2 The graphics context
	 */
	@Override
	public void draw(Graphics2D g2)
	{
		super.draw(g2);

		if (_drawContent)
		{
			Point2D.Float source = new Point2D.Float();
			_source.getControlPosition(source);

			var tc = getTargetConnectors();
			Point2D.Float avgTarget = Geometry.averagePointFloat(
					tc.stream().map(c -> {
						Point2D.Float pt = new Point2D.Float();
						c.getControlPosition(pt);
						return pt;
					}), tc.size());
			float radAngle = Geometry.getAngle(source, avgTarget);

			g2 = (Graphics2D) g2.create();
			try
			{
				g2.translate(source.x, source.y);
				g2.rotate(radAngle);
				g2.translate(15, 0);
				g2.rotate(-radAngle);

				g2.setPaint(_context._style.getTextPaint());

				FontMetrics fm = _context._style.getFontMetrics();
				int y = 0;
				if (_events != null)
				{
					if (_condition != null)
						y -= fm.getDescent();
					g2.drawString(_events, 0, y);
					y += fm.getHeight();
				}
				if (_condition != null)
					g2.drawString(_condition, 0, y);
			}
			finally
			{
				g2.dispose();
			}
		}
	}
}

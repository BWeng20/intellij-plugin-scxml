package com.bw.modelthings.fsm.ui;

import com.bw.graph.DrawContext;
import com.bw.graph.primitive.Circle;
import com.bw.graph.visual.GenericPrimitiveVisual;
import com.bw.graph.visual.VisualFlags;

/**
 * Visual to show a start-state.
 */
public class StartVisual extends GenericPrimitiveVisual
{
	/**
	 * The parent visual that contains the model of this start-state.
	 */
	public StateVisual _parent;

	/**
	 * Create a new start node visual.
	 *
	 * @param parent  The parent state.
	 * @param context The Drawing context to use.
	 */
	public StartVisual(StateVisual parent, DrawContext context)
	{
		super(parent._state._docId, context);
		_parent = parent;
	}

	/**
	 * Creates the primitives.
	 *
	 * @param x      Base X Position
	 * @param y      Base Y Position
	 * @param radius The radius of the circle.
	 * @param style  The drawing style to use.
	 * @param bounds Outer bounds. If null bounds will be calculated.
	 */
	public void createPrimitives(float x, float y, float radius, PosAndBounds bounds, DrawContext style)
	{
		Circle circle = new Circle(0, 0, radius, style._configuration, style._style, VisualFlags.ALWAYS);
		circle.setFill(true);
		addDrawingPrimitive(circle);
		Circle circleActive = new Circle(0, 0, radius + 5, style._configuration, style._style, VisualFlags.SELECTED);
		addDrawingPrimitive(circleActive);
		clearFlags(VisualFlags.MODIFIED);
		setFlags(FsmVisualFlags.START_NODE_FLAG);

		if (bounds == null)
		{
			setAbsolutePosition(x, y, null);
		}
		else
		{
			setAbsolutePosition(bounds.position, bounds.bounds);
		}
	}
}

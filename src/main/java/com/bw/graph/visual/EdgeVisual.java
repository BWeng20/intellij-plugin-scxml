package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.Path;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.LinkedList;

/**
 * Used to visualize edges in different modes.<br>
 * A path have a line (along the path), one connector at each end, and a number of control-points.
 */
public class EdgeVisual extends Visual
{
	/**
	 * The source connector at the source visual where the edge starts.
	 */
	protected ConnectorVisual sourceConnector;

	/**
	 * The target connector at the target visual where the edge ends.
	 */
	protected ConnectorVisual targetConnector;

	/**
	 * The path to draw
	 */
	protected Path path;

	/**
	 * The list of control points.
	 */
	protected java.util.List<PathControlVisual> controlVisual = new LinkedList<>();

	/**
	 * Creates a new Primitive.
	 *
	 * @param id      The Identification, can be null.
	 * @param source  The start connector.
	 * @param target  The end connector.
	 * @param context The  context. Must not be null.
	 */
	public EdgeVisual(Object id, ConnectorVisual source, ConnectorVisual target,
					  DrawContext context)
	{
		super(id, context);

		this.path = new Path(context.configuration, context.normal);
		this.sourceConnector = source;
		this.targetConnector = target;

		sourceConnector.setEdgeVisual(this);
		targetConnector.setEdgeVisual(this);

		this.path.addPoint(sourceConnector);
		this.path.addPoint(targetConnector);
	}

	/**
	 * Draws for given context.
	 *
	 * @param g2 The graphics context
	 */
	@Override
	public void draw(Graphics2D g2)
	{
		if (targetConnector != null)
		{
			path.draw(g2);
			targetConnector.draw(g2);
		}
		sourceConnector.draw(g2);
	}

	@Override
	protected void drawRelative(Graphics2D g2)
	{

	}

	@Override
	public DrawPrimitive getEditablePrimitiveAt(float x, float y)
	{
		DrawPrimitive p = path;
		if (targetConnector != null)
			p = targetConnector.getEditablePrimitiveAt(x, y);
		if (p == null && sourceConnector != null)
			p = sourceConnector.getEditablePrimitiveAt(x, y);
		if (p == null)
		{
			p = path;
			if (p != null)
				p.setVisual(this);
		}
		return p;
	}

	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		absoluteBounds.width = 0;
		absoluteBounds.height = 0;
	}

	@Override
	public void toSVG(SVGWriter sw, Graphics2D g2)
	{
		path.toSVG(sw, g2, new Point2D.Float(0, 0));
	}

	@Override
	public void dispose()
	{
		super.dispose();
		path = null;
		if (sourceConnector != null)
		{
			sourceConnector.dispose();
			sourceConnector = null;
		}
		if (targetConnector != null)
		{
			targetConnector.dispose();
			targetConnector = null;
		}
		controlVisual.forEach(Visual::dispose);
		controlVisual.clear();
	}

	/**
	 * Checks if a point is inside the area of the visual.
	 *
	 * @param x X position.
	 * @param y Y position.
	 * @return true if (x,y) is inside the visual.
	 */
	public boolean containsPoint(float x, float y)
	{
		return path.getDistanceTo(new Point2D.Float(x, y)) < context.configuration.selectEdgeMaxDistance;
	}


	public <T extends DrawPrimitive> T getPrimitiveOf(Class<T> primitiveClass)
	{
		return null;
	}


	@Override
	public void moveBy(float x, float y)
	{
	}
}
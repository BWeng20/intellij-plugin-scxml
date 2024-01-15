package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
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

		this.path = new Path(context.configuration, null);
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
	protected void drawIntern(Graphics2D g2, DrawStyle actualStyle)
	{
		if (targetConnector != null)
		{
			path.draw(g2, null, actualStyle);
			targetConnector.draw(g2, actualStyle);
		}
		sourceConnector.draw(g2, actualStyle);

	}

	@Override
	protected void updateBounds(Graphics2D graphics)
	{
		x2 = position.x;
		y2 = position.y;
	}

	@Override
	public void toSVG(SVGWriter sw, Graphics2D g2)
	{
		// @TODO
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

}
package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.primitive.Path;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
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
	 * @param source  The start connector.
	 * @param target  The end connector.
	 * @param context The  context. Must not be null.
	 */
	public EdgeVisual(ConnectorVisual source, ConnectorVisual target,
					  DrawContext context)
	{
		super(context);

		this.path = new Path(context.configuration, context.normal);
		this.sourceConnector = source;
		this.targetConnector = target;
	}

	/**
	 * Draws for given context.
	 *
	 * @param g2 The graphics context
	 */
	public void draw(Graphics2D g2, DrawStyle actualStyle)
	{
		if (targetConnector != null)
		{
			g2.setPaint(actualStyle.linePaint);
			g2.setStroke(actualStyle.lineStroke);
			g2.draw(new Line2D.Float(sourceConnector.getCenterPosition(), targetConnector.getCenterPosition()));
			// path.draw(g2, null, actualStyle);

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
}
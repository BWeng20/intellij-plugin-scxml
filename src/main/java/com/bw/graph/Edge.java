package com.bw.graph;

import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * Edge that connects two visuals.
 */
public class Edge
{

	/**
	 * Start visual.
	 */
	public Visual start;

	/**
	 * Target  visual.
	 */
	public Visual end;

	/**
	 * The drawing context to use for painting and size calculations.
	 */
	protected DrawContext context;

	/**
	 * Creates a new Edge.
	 *
	 * @param start   Start Visual. Must not be null.
	 * @param end     Target Visual. Must not be null.
	 * @param context Draw context. Must not be null.
	 */
	public Edge(Visual start, Visual end, DrawContext context)
	{
		Objects.requireNonNull(start);
		Objects.requireNonNull(end);
		Objects.requireNonNull(context);
		this.start = start;
		this.end = end;
		this.context = context;
	}

	/**
	 * Draw the edge.
	 *
	 * @param g2 The Graphics context
	 */
	public void draw(Graphics2D g2)
	{
		g2.setStroke(context.normal.lineStroke);
		g2.setPaint(context.normal.linePaint);

		Rectangle2D.Float startB = start.getBounds2D(g2);
		Rectangle2D.Float endB = end.getBounds2D(g2);

		Line2D line = new Line2D.Double(startB.getCenterX(), startB.getCenterY(), endB.getCenterX(), endB.getCenterY());
		g2.draw(line);
	}
}
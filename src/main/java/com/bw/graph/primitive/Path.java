package com.bw.graph.primitive;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.Geometry;
import com.bw.jtools.svg.ShapeHelper;
import com.bw.svg.SVGAttribute;
import com.bw.svg.SVGElement;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

/**
 * A Path.
 */
public class Path extends DrawPrimitive
{

	/**
	 * Edge mode.
	 */
	public enum Mode
	{
		/**
		 * Quad
		 */
		Quad,
		/**
		 * Simple line
		 */
		Straight,

	}

	/**
	 * Edge mode.
	 */
	protected Mode mode = Mode.Quad;

	/**
	 * Control points.
	 */
	protected List<PathControlPoint> controlPoints = new ArrayList<>();

	/**
	 * The edge path, created during draw.
	 */
	protected Path2D path2D;

	/**
	 * The buffered translated arrow shape for the path end.
	 */
	protected Shape arrowEndTranslated;

	/**
	 * Buffered control point coordinates.
	 */
	protected Point2D.Float[] coordinates = new Point2D.Float[0];

	/**
	 * Reused arrow path template.
	 */
	protected Path2D arrow = new Path2D.Float();

	/**
	 * Creates a new Path Primitive.<br>
	 * Control Points are dynamically created and bound.
	 *
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 */
	public Path(GraphConfiguration config,
				DrawStyle style)
	{
		super(0, 0, config, style);

		arrow.moveTo(-2f * config.connectorSize, -config.connectorSize);
		arrow.lineTo(0, 0);
		arrow.lineTo(-2f * config.connectorSize, config.connectorSize);
		arrow.closePath();
	}

	/**
	 * Draws for given context.<br>
	 * Paths don't use relative positions or insets.
	 * This override draws a path along the absolute points of the control points.
	 *
	 * @param g2          The graphics context
	 * @param position    not used.
	 * @param parentStyle The style of parent, used if primitive has no own style.
	 */
	@Override
	public void draw(Graphics2D g2)
	{
		Point2D.Float pt = new Point2D.Float();
		boolean recreatePath = path2D == null;

		final int LI = coordinates.length - 1;

		for (int i = 0; i <= LI; ++i)
		{
			if (recreatePath)
			{
				controlPoints.get(i)
							 .getControlPosition(coordinates[i]);
			}
			else
			{
				controlPoints.get(i)
							 .getControlPosition(pt);
				if (pt.x != coordinates[i].x || pt.y != coordinates[i].y)
				{
					recreatePath = true;
					coordinates[i].x = pt.x;
					coordinates[i].y = pt.y;
				}
			}
		}
		if (recreatePath)
		{
			path2D = new Path2D.Float();
			arrowEndTranslated = null;

			if (LI >= 0)
			{
				path2D.moveTo(coordinates[0].x, coordinates[0].y);
				if (LI > 0)
				{
					for (int i = 1; i <= LI; ++i)
					{
						switch (mode)
						{
							case Quad ->
							{
								float cx = coordinates[i - 1].x + (coordinates[i].x - coordinates[i - 1].x) / 2f;
								float cy = coordinates[i].y;
								path2D.quadTo(cx, cy, coordinates[i].x, coordinates[i].y);
							}
							case Straight -> path2D.lineTo(coordinates[i].x, coordinates[i].y);
						}
					}
					ShapeHelper sh = new ShapeHelper(path2D);
					var pos = sh.pointAtLength(sh.getOutlineLength() - config.connectorSize);
					double theta = pos == null ? 0 : pos.angle_;

					AffineTransform aft = new AffineTransform();
					aft.translate(coordinates[LI].x, coordinates[LI].y);
					aft.rotate(theta);
					arrowEndTranslated = aft.createTransformedShape(arrow);
				}
			}
		}

		g2.setStroke(style.lineStroke);
		g2.setPaint(style.linePaint);
		g2.draw(path2D);
		if (arrowEndTranslated != null)
			g2.fill(arrowEndTranslated);
	}

	@Override
	protected void drawIntern(Graphics2D g2)
	{
		// unused
	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D graphics)
	{
		return new Dimension2DFloat(0, 0);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float pos)
	{
		final int LI = coordinates.length - 1;
		for (int i = 0; i <= LI; ++i)
		{
			controlPoints.get(i)
						 .getControlPosition(coordinates[i]);
		}
		StringBuilder pathB = new StringBuilder();
		pathB.append('M');
		appendPoint(pathB, coordinates[0]);

		Point2D.Float pt = new Point2D.Float();
		for (int i = 1; i <= LI; ++i)
		{
			switch (mode)
			{
				case Quad ->
				{
					pt.x = coordinates[i - 1].x + (coordinates[i].x - coordinates[i - 1].x) / 2f;
					pt.y = coordinates[i].y;
					pathB.append('Q');
					appendPoint(pathB, pt);
					pathB.append(' ');
					appendPoint(pathB, coordinates[i]);
				}
				case Straight ->
				{
					pathB.append('L');
					appendPoint(pathB, coordinates[i]);
				}
			}
		}
		sw.startElement(SVGElement.g);
		sw.startElement(SVGElement.path);
		sw.writeAttribute(SVGAttribute.Fill, (Paint) null);
		sw.writeAttribute(SVGAttribute.Stroke, style.linePaint);
		sw.writeStrokeWidth(style.getStrokeWidth());
		sw.writeAttribute(SVGAttribute.D, pathB.toString());
		sw.endElement();
		sw.writeShape(arrowEndTranslated, 1, style.linePaint, null, 0, true);
		sw.endElement();
	}

	static final float precisionFactor = 10 * 10 * 10;

	private void appendPoint(StringBuilder pathB, Point2D.Float pt)
	{
		pathB.append(SVGWriter.floatToStringRestrictedPrecision(pt.x, precisionFactor))
			 .append(' ')
			 .append(SVGWriter.floatToStringRestrictedPrecision(pt.y, precisionFactor));
	}

	/**
	 * Adds a point to the path.
	 *
	 * @param pt The new point
	 */
	public void addPoint(PathControlPoint pt)
	{
		controlPoints.add(pt);
		path2D = null;

		coordinates = new Point2D.Float[controlPoints.size()];
		for (int i = 0; i < coordinates.length; ++i)
			coordinates[i] = new Point2D.Float(0, 0);
	}

	/**
	 * Get the shortest distance from path to the point.
	 *
	 * @param pt The reference point.
	 * @return The calculated distance.
	 */
	public float getDistanceTo(Point2D.Float pt)
	{
		if (pt != null && coordinates.length > 1)
		{
			return (float) pt.distance(Geometry.getClosestPointOnShape(pt, path2D, 1));
		}
		return Float.MAX_VALUE;
	}
}
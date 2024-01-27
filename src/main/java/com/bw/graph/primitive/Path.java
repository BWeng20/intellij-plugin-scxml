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
	protected Mode _mode = Mode.Quad;

	/**
	 * Control points.
	 */
	protected List<PathControlPoint> _controlPoints = new ArrayList<>();

	/**
	 * The edge path, created during draw.
	 */
	protected Path2D _path2D;

	/**
	 * The buffered translated arrow shape for the path end.
	 */
	protected Shape _arrowEndTranslated;

	/**
	 * Buffered control point coordinates.
	 */
	protected Point2D.Float[] _coordinates = new Point2D.Float[0];

	/**
	 * Reused arrow path template.
	 */
	protected Path2D _arrow = new Path2D.Float();

	/**
	 * Creates a new Path Primitive.<br>
	 * Control Points are dynamically created and bound.
	 *
	 * @param config The configuration to use.
	 * @param style  The style or null if default style shall be used.
	 * @param flags  Bitwise combination of flag-bits.
	 */
	public Path(GraphConfiguration config,
				DrawStyle style, int flags)
	{
		super(0, 0, config, style, flags);

		_arrow.moveTo(-2f * config._connectorSize, -config._connectorSize);
		_arrow.lineTo(0, 0);
		_arrow.lineTo(-2f * config._connectorSize, config._connectorSize);
		_arrow.closePath();
	}

	/**
	 * Draws for given context.<br>
	 * Paths don't use relative positions or insets.
	 * This override draws a path along the absolute points of the control points.
	 *
	 * @param g2 The graphics context
	 */
	@Override
	public void draw(Graphics2D g2)
	{
		Point2D.Float pt = new Point2D.Float();
		boolean recreatePath = _path2D == null;

		final int LI = _coordinates.length - 1;

		for (int i = 0; i <= LI; ++i)
		{
			if (recreatePath)
			{
				_controlPoints.get(i)
							  .getControlPosition(_coordinates[i]);
			}
			else
			{
				_controlPoints.get(i)
							  .getControlPosition(pt);
				if (pt.x != _coordinates[i].x || pt.y != _coordinates[i].y)
				{
					recreatePath = true;
					_coordinates[i].x = pt.x;
					_coordinates[i].y = pt.y;
				}
			}
		}
		if (recreatePath)
		{
			_path2D = new Path2D.Float();
			_arrowEndTranslated = null;

			if (LI >= 0)
			{
				_path2D.moveTo(_coordinates[0].x, _coordinates[0].y);
				if (LI > 0)
				{
					for (int i = 1; i <= LI; ++i)
					{
						switch (_mode)
						{
							case Quad ->
							{
								float cx = _coordinates[i - 1].x + (_coordinates[i].x - _coordinates[i - 1].x) / 2f;
								float cy = _coordinates[i].y;
								_path2D.quadTo(cx, cy, _coordinates[i].x, _coordinates[i].y);
							}
							case Straight -> _path2D.lineTo(_coordinates[i].x, _coordinates[i].y);
						}
					}
					ShapeHelper sh = new ShapeHelper(_path2D);
					var pos = sh.pointAtLength(sh.getOutlineLength() - _config._connectorSize);
					double theta = pos == null ? 0 : pos.angle_;

					AffineTransform aft = new AffineTransform();
					aft.translate(_coordinates[LI].x, _coordinates[LI].y);
					aft.rotate(theta);
					_arrowEndTranslated = aft.createTransformedShape(_arrow);
				}
			}
		}

		g2.setStroke(_style._lineStroke);
		g2.setPaint(_style._linePaint);
		g2.draw(_path2D);
		if (_arrowEndTranslated != null)
			g2.fill(_arrowEndTranslated);
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
		final int LI = _coordinates.length - 1;
		for (int i = 0; i <= LI; ++i)
		{
			_controlPoints.get(i)
						  .getControlPosition(_coordinates[i]);
		}
		StringBuilder pathB = new StringBuilder();
		pathB.append('M');
		appendPoint(pathB, _coordinates[0]);

		Point2D.Float pt = new Point2D.Float();
		for (int i = 1; i <= LI; ++i)
		{
			switch (_mode)
			{
				case Quad ->
				{
					pt.x = _coordinates[i - 1].x + (_coordinates[i].x - _coordinates[i - 1].x) / 2f;
					pt.y = _coordinates[i].y;
					pathB.append('Q');
					appendPoint(pathB, pt);
					pathB.append(' ');
					appendPoint(pathB, _coordinates[i]);
				}
				case Straight ->
				{
					pathB.append('L');
					appendPoint(pathB, _coordinates[i]);
				}
			}
		}
		sw.startElement(SVGElement.g);
		sw.startElement(SVGElement.path);
		sw.writeAttribute(SVGAttribute.Fill, (Paint) null);
		sw.writeAttribute(SVGAttribute.Stroke, _style._linePaint);
		sw.writeStrokeWidth(_style.getStrokeWidth());
		sw.writeAttribute(SVGAttribute.D, pathB.toString());
		sw.endElement();
		sw.writeShape(_arrowEndTranslated, 1, _style._linePaint, null, 0);
		sw.endElement();
	}

	static final float _precisionFactor = 10 * 10 * 10;

	private void appendPoint(StringBuilder pathB, Point2D.Float pt)
	{
		pathB.append(SVGWriter.floatToString(pt.x, _precisionFactor))
			 .append(' ')
			 .append(SVGWriter.floatToString(pt.y, _precisionFactor));
	}

	/**
	 * Adds a point to the path.
	 *
	 * @param pt The new point
	 */
	public void addPoint(PathControlPoint pt)
	{
		_controlPoints.add(pt);
		_path2D = null;

		_coordinates = new Point2D.Float[_controlPoints.size()];
		for (int i = 0; i < _coordinates.length; ++i)
			_coordinates[i] = new Point2D.Float(0, 0);
	}

	/**
	 * Get the shortest distance from path to the point.
	 *
	 * @param pt The reference point.
	 * @return The calculated distance.
	 */
	public float getDistanceTo(Point2D.Float pt)
	{
		if (pt != null && _coordinates.length > 1)
		{
			return (float) pt.distance(Geometry.getClosestPointOnShape(pt, _path2D, 1));
		}
		return Float.MAX_VALUE;
	}
}
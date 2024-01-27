package com.bw.graph.primitive;

import com.bw.XmlWriter;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.VisualModel;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.visual.Visual;
import com.bw.graph.visual.VisualFlags;
import com.bw.svg.SVGAttribute;
import com.bw.svg.SVGElement;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * Primitive that carries a Sub-Model. The primitive draws a thumb of it.
 */
public class ModelPrimitive extends DrawPrimitive
{
	/**
	 * A sub-model.
	 */
	protected VisualModel _childModel;


	/**
	 * Creates a new ModelPrimitive.
	 *
	 * @param x      The relative x-position
	 * @param y      The relative y-position
	 * @param config The configuration to use.
	 * @param style  The local style or null if parent style shall be used.
	 * @param flags  The initial flags. @see {@link VisualFlags}
	 */
	public ModelPrimitive(float x, float y, GraphConfiguration config, DrawStyle style, int flags)
	{
		super(x, y, config, style, flags);
	}

	/**
	 * Sets the child model.
	 *
	 * @param model The model or null
	 */
	public void setChildModel(VisualModel model)
	{
		_childModel = model;
	}

	@Override
	public void repaint()
	{
		super.repaint();
		if (_childModel != null)
			_childModel.repaint();
	}

	/**
	 * Gets the child model.
	 *
	 * @return The model or null
	 */
	public VisualModel getChildModel()
	{
		return _childModel;
	}

	@Override
	protected void drawIntern(Graphics2D g2)
	{
		if (_childModel != null)
		{
			Rectangle2D.Float subBounds = _childModel.getBounds2D(g2);
			Rectangle2D.Float subModelBox = getInnerDimension(g2).getBounds();

			float innerInset2 = 10;

			// Calc scale, use minimum to keep aspect ratio
			float scale = Math.min((subModelBox.width - innerInset2) / subBounds.width, (subModelBox.height - innerInset2) / subBounds.height);
			if (scale > 1f)
				scale = 1f;

			g2.setPaint(_style._background);
			g2.fill(subModelBox);
			g2.setStroke(_style._lineStroke);
			g2.setPaint(_style._linePaint);
			g2.draw(subModelBox);

			AffineTransform orgAft = g2.getTransform();
			try
			{
				g2.translate((subModelBox.width - subBounds.width * scale) / 2f,
						(subModelBox.height - subBounds.height * scale) / 2f);
				g2.scale(scale, scale);
				_childModel.draw(g2);
			}
			finally
			{
				g2.setTransform(orgAft);
			}
		}

	}

	@Override
	protected Dimension2DFloat getInnerDimension(Graphics2D g2)
	{
		if (_childModel != null)
		{
			return new Dimension2DFloat(_config._innerModelBoxMinDimension);
		}
		return new Dimension2DFloat(0, 0);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float basePos)
	{
		if (_childModel != null)
		{
			sw.startElement(SVGElement.g);

			Rectangle2D.Float subBounds = _childModel.getBounds2D(g2);
			Rectangle2D.Float subModelBox = getInnerDimension(g2).getBounds();

			float innerInset2 = 10;

			// Calc scale, use minimum to keep aspect ratio
			float scale = Math.min((subModelBox.width - innerInset2) / subBounds.width, (subModelBox.height - innerInset2) / subBounds.height);
			// Don't zoom bigger.
			if (scale > 1f)
				scale = 1f;

			float offsetX = basePos.x + (subModelBox.width - subBounds.width * scale) / 2f;
			float offsetY = basePos.y + (subModelBox.height - subBounds.height * scale) / 2f;

			sw.writeAttribute(SVGAttribute.Transform,
					"translate(" + XmlWriter.floatToString(offsetX, _config._precisionFactor)
							+ " " + XmlWriter.floatToString(offsetY, _config._precisionFactor) + ") scale("
							+ XmlWriter.floatToString(scale, _config._precisionFactor) + ")");

			for (Visual v : _childModel.getVisuals())
			{
				v.toSVG(sw, g2);
			}
			sw.endElement();
		}
	}

	@Override
	public void dispose()
	{
		if (_childModel != null)
		{
			_childModel.dispose();
			_childModel = null;
		}
		super.dispose();
	}

	@Override
	public boolean isModified()
	{
		return _childModel != null && _childModel.isModified();
	}

	/**
	 * Sets the modified state of the sub-model. Has no effect if no sub-model is set.
	 *
	 * @param modified The new modified state.
	 */
	@Override
	public void setModified(boolean modified)
	{
		if (_childModel != null)
			_childModel.setModified(modified);
	}


	/**
	 * Checks if the visual contains a child model.
	 *
	 * @param visual The visual to check.
	 * @return True if the visual contains a child model.
	 */
	public static boolean hasChildModel(Visual visual)
	{
		return getChildModel(visual) != null;
	}

	/**
	 * Gets a child model from visual.
	 *
	 * @param visual The visual.
	 * @return The child model or null.
	 */
	public static VisualModel getChildModel(Visual visual)
	{
		if (visual != null)
		{
			ModelPrimitive modelPrimitive = visual.getPrimitiveOf(ModelPrimitive.class);
			if (modelPrimitive != null)
				return modelPrimitive.getChildModel();
		}
		return null;
	}

}

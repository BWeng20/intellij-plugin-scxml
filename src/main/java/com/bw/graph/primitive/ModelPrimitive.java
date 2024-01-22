package com.bw.graph.primitive;

import com.bw.XmlWriter;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.VisualModel;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.visual.Visual;
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
	protected VisualModel subModel;


	/**
	 * Creates a new ModelPrimitive.
	 *
	 * @param x      The relative x-position
	 * @param y      The relative y-position
	 * @param config The configuration to use.
	 * @param style  The local style or null if parent style shall be used.
	 */
	public ModelPrimitive(float x, float y, GraphConfiguration config, DrawStyle style)
	{
		super(x, y, config, style);
	}

	/**
	 * Sets the sub model.
	 *
	 * @param model The model or null
	 */
	public void setSubModel(VisualModel model)
	{
		subModel = model;
	}

	@Override
	public void repaint()
	{
		super.repaint();
		if (subModel != null)
			subModel.repaint();
	}

	/**
	 * Gets the sub-model.
	 *
	 * @return The model or null
	 */
	public VisualModel getSubModel()
	{
		return subModel;
	}

	@Override
	protected void drawIntern(Graphics2D g2)
	{
		if (subModel != null)
		{
			Rectangle2D.Float subBounds = subModel.getBounds2D(g2);
			Rectangle2D.Float subModelBox = getInnerDimension(g2).getBounds();

			float innerInset2 = 10;

			// Calc scale, use minimum to keep aspect ratio
			float scale = Math.min((subModelBox.width - innerInset2) / subBounds.width, (subModelBox.height - innerInset2) / subBounds.height);
			if (scale > 1f)
				scale = 1f;

			g2.setPaint(style.background);
			g2.fill(subModelBox);
			g2.setStroke(style.lineStroke);
			g2.setPaint(style.linePaint);
			g2.draw(subModelBox);

			AffineTransform orgAft = g2.getTransform();
			try
			{
				g2.translate((subModelBox.width - subBounds.width * scale) / 2f,
						(subModelBox.height - subBounds.height * scale) / 2f);
				g2.scale(scale, scale);
				subModel.draw(g2);
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
		if (subModel != null)
		{
			return new Dimension2DFloat(config.innerModelBoxMinDimension);
		}
		return new Dimension2DFloat(0, 0);
	}

	@Override
	protected void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float basePos)
	{
		if (subModel != null)
		{
			sw.startElement(SVGElement.g);

			Rectangle2D.Float subBounds = subModel.getBounds2D(g2);
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
					"translate(" + XmlWriter.floatToString(offsetX, config.precisionFactor)
							+ " " + XmlWriter.floatToString(offsetY, config.precisionFactor) + ") scale("
							+ XmlWriter.floatToString(scale, config.precisionFactor) + ")");

			for (Visual v : subModel.getVisuals())
			{
				v.toSVG(sw, g2);
			}
			sw.endElement();
		}
	}

	@Override
	public void dispose()
	{
		if (subModel != null)
		{
			subModel.dispose();
			subModel = null;
		}
		super.dispose();
	}

	@Override
	public boolean isModified()
	{
		return subModel != null && subModel.isModified();
	}

	/**
	 * Sets the modified state of the sub-model. Has no effect if no sub-model is set.
	 *
	 * @param modified The new modified state.
	 */
	@Override
	public void setModified(boolean modified)
	{
		if (subModel != null)
			subModel.setModified(modified);
	}


	/**
	 * Checks if the visual contains a sub-model.
	 *
	 * @param visual The visual to check.
	 * @return True if the visual contains a sub-model.
	 */
	public static boolean hasSubModel(Visual visual)
	{
		return getSubModel(visual) != null;
	}

	/**
	 * Gets a sub-model from visual.
	 *
	 * @param visual The visual.
	 * @return The sub-model or null.
	 */
	public static VisualModel getSubModel(Visual visual)
	{
		if (visual != null)
		{
			ModelPrimitive modelPrimitive = visual.getPrimitiveOf(ModelPrimitive.class);
			if (modelPrimitive != null)
				return modelPrimitive.getSubModel();
		}
		return null;
	}

}

package com.bw.modelthings.fsm.ui;

import com.bw.graph.Alignment;
import com.bw.graph.DrawContext;
import com.bw.graph.editor.EditorProxy;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.Line;
import com.bw.graph.primitive.Rectangle;
import com.bw.graph.primitive.Text;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.InsetsFloat;
import com.bw.graph.visual.GenericVisual;
import com.bw.graph.visual.Visual;
import com.bw.modelthings.fsm.model.State;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * Proxy to maintain a state visual.
 * One instance of this proxy correspond to one state instance
 * but can maintain multiple representing visuals.
 */
public class StateNameProxy implements EditorProxy
{
	/**
	 * The handled state.
	 */
	public State state;

	/**
	 * The editor component.
	 */
	public JTextComponent textComponent;

	/**
	 * The original name of the state from start of edit.
	 */
	private String originalName;

	/**
	 * State outer draw context.
	 */
	private DrawContext stateOuterContext;

	/**
	 * The draw context for inner primitives.
	 */
	private DrawContext stateInnerContext;

	/**
	 * Create a new state proxy.
	 *
	 * @param state             The state to handle.
	 * @param textComponent     The text editor component.
	 * @param stateOuterContext The draw context for outlines.
	 * @param stateInnerContext The draw context for inner primitives.
	 */
	public StateNameProxy(State state, JTextComponent textComponent, DrawContext stateOuterContext, DrawContext stateInnerContext)
	{
		this.state = state;
		this.textComponent = textComponent;
		this.stateOuterContext = stateOuterContext;
		this.stateInnerContext = stateInnerContext;
	}

	@Override
	public String toString()
	{
		return state.name;
	}

	@Override
	public JComponent getEditor(DrawPrimitive text)
	{
		originalName = state.name;
		textComponent.setText(state.name);
		return textComponent;
	}

	@Override
	public void endEdit(DrawPrimitive text, Graphics2D g2)
	{
		String newName = textComponent.getText().trim();
		if (!Objects.equals(newName, state.name))
		{
			if (text instanceof Text)
			{
				((Text) text).setText(newName);
			}
			state.name = textComponent.getText();
			Visual v = text.getVisual();
			v.setId(newName);
			v.setModified(true);
			Point2D.Float pt = v.getPosition();
			createStatePrimitives(v, pt.x, pt.y, g2, null);
			v.setPreferredDimension(null);
		}
	}

	@Override
	public void cancelEdit(DrawPrimitive text)
	{
		if (originalName != null && !Objects.equals(originalName, state.name))
		{
			state.name = originalName;
			text.getVisual().setModified(true);
		}
	}

	/**
	 * Creates the primitives for a state visual.
	 *
	 * @param visual The visual of the state.
	 * @param x      Base X Position
	 * @param y      Base Y Position
	 * @param g2     Graphics to use for dimension calculations.
	 * @param bounds Outer bounds. If null bounds will be calculated.
	 */
	public void createStatePrimitives(Visual visual, float x, float y, Graphics2D g2,
									  Rectangle2D.Float bounds)
	{
		GenericVisual v = (GenericVisual) visual;
		v.removeAllDrawingPrimitives();

		float fh = stateInnerContext.normal.fontMetrics.getHeight();

		if (bounds == null)
		{
			Rectangle2D stringBounds = stateInnerContext.normal.fontMetrics.getStringBounds(state.name, g2);

			float height = 5 * fh;
			float width = (float) Math.max(stringBounds.getWidth() + 10, stateInnerContext.configuration.stateMinimalWidth);

			if (visual.getSubModel() != null)
			{
				// For a state with internal states (a sub fsm in this context)
				// we need space for the small image of the inner fsm.
				Dimension2DFloat dim = stateInnerContext.configuration.innerModelBoxMinDimension;
				InsetsFloat insets = stateInnerContext.configuration.innerModelBoxInsets;

				float w = dim.width + insets.right + insets.left;
				float h = dim.height + insets.top + insets.bottom;

				if (width < w) width = w;
				if (height < h) height = h;
			}
			bounds = new Rectangle2D.Float(x, y, width, height);
		}
		else
		{
			v.setPreferredDimension(bounds.width, bounds.height);
		}
		v.setPosition(bounds.x, bounds.y);

		Rectangle frame = new Rectangle(
				0, 0, bounds.width, bounds.height, stateOuterContext.configuration.stateCornerArcSize, stateOuterContext.configuration,
				stateOuterContext.normal);

		frame.setFill(true);
		v.addDrawingPrimitive(frame);

		Line separator = new Line(0, fh * 1.5f, bounds.width, fh * 1.5f
				, stateInnerContext.configuration, stateInnerContext.normal);
		v.addDrawingPrimitive(separator);

		Text label = new Text(0, 0, state.name, stateInnerContext.configuration, stateInnerContext.normal);
		label.setEditable(true);
		label.setAlignment(Alignment.Center);
		label.setInsets(fh * 0.25f, 0, 0, 0);
		label.setUserData(this);
		v.addDrawingPrimitive(label);
	}

}

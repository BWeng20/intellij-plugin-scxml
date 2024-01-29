package com.bw.modelthings.fsm.ui;

import com.bw.graph.Alignment;
import com.bw.graph.DrawContext;
import com.bw.graph.editor.EditorProxy;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.Line;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.primitive.Rectangle;
import com.bw.graph.primitive.Text;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.InsetsFloat;
import com.bw.graph.visual.GenericPrimitiveVisual;
import com.bw.graph.visual.Visual;
import com.bw.graph.visual.VisualFlags;
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
	public State _state;

	/**
	 * The editor component.
	 */
	public JTextComponent _textComponent;

	/**
	 * The name of the state before edit.
	 */
	private String _nameBeforeEdit;

	/**
	 * The name of the state in file.
	 */
	public String _nameInFile;


	/**
	 * State outer draw context.
	 */
	private DrawContext _stateOuterContext;

	/**
	 * The draw context for inner primitives.
	 */
	private DrawContext _stateInnerContext;

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
		this._state = state;
		this._textComponent = textComponent;
		this._stateOuterContext = stateOuterContext;
		this._stateInnerContext = stateInnerContext;
		this._nameInFile = state._name;
	}

	@Override
	public String toString()
	{
		return _state._name;
	}

	@Override
	public JComponent getEditor(DrawPrimitive text)
	{
		_nameBeforeEdit = _state._name;
		_textComponent.setText(_state._name);
		return _textComponent;
	}

	@Override
	public void endEdit(DrawPrimitive text, Graphics2D g2)
	{
		String newName = _textComponent.getText()
									   .trim();
		if (!Objects.equals(newName, _state._name))
		{
			if (text instanceof Text)
			{
				((Text) text).setText(newName);
			}
			_state._name = _textComponent.getText();
			GenericPrimitiveVisual v = (GenericPrimitiveVisual) text.getVisual();
			v.setId(newName);
			v.setFlags(VisualFlags.MODIFIED);
			Point2D.Float pt = v.getAbsolutePosition();
			createStatePrimitives(v, pt.x, pt.y, g2, null);
			v.setPreferredDimension(null);
		}
	}

	@Override
	public void cancelEdit(DrawPrimitive text)
	{
		if (_nameBeforeEdit != null && !Objects.equals(_nameBeforeEdit, _state._name))
		{
			_state._name = _nameBeforeEdit;
			text.getVisual()
				.setFlags(VisualFlags.MODIFIED);
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
									  GraphExtension.PosAndBounds bounds)
	{
		GenericPrimitiveVisual v = (GenericPrimitiveVisual) visual;
		float fh = _stateInnerContext._style._fontMetrics.getHeight();

		ModelPrimitive modelPrimitive = visual.getPrimitiveOf(ModelPrimitive.class);

		v.removeAllDrawingPrimitives();
		if (bounds == null)
		{
			Rectangle2D stringBounds = _stateInnerContext._style._fontMetrics.getStringBounds(_state._name, g2);

			float height = 5 * fh;
			float width = (float) Math.max(stringBounds.getWidth() + 10, _stateInnerContext._configuration._stateMinimalWidth);

			if (modelPrimitive != null)
			{
				// For a state with internal states (a sub fsm in this context)
				// we need space for the small image of the inner fsm.
				Dimension2DFloat dim = _stateInnerContext._configuration._innerModelBoxMinDimension;
				InsetsFloat insets = _stateInnerContext._configuration._innerModelBoxInsets;

				float w = dim._width + insets._right + insets._left;
				float h = dim._height + insets._top + insets._bottom;

				if (width < w) width = w;
				if (height < h) height = h;
			}
			bounds = new GraphExtension.PosAndBounds(new Point2D.Float(x, y), new Rectangle2D.Float(x, y, width, height));
		}
		v.setAbsolutePosition(bounds.position, bounds.bounds);

		Rectangle frame = new Rectangle(
				0, 0, bounds.bounds.width, bounds.bounds.height, _stateOuterContext._configuration._stateCornerArcSize, _stateOuterContext._configuration,
				_stateOuterContext._style, VisualFlags.ALWAYS);

		frame.setFill(true);
		v.addDrawingPrimitive(frame);

		Line separator = new Line(0, fh * 1.5f, bounds.bounds.width, fh * 1.5f
				, _stateInnerContext._configuration, _stateInnerContext._style, VisualFlags.ALWAYS);
		v.addDrawingPrimitive(separator);

		Text label = new Text(0, 0, _state._name, _stateInnerContext._configuration, _stateInnerContext._style, VisualFlags.ALWAYS);
		label.setFlags(VisualFlags.EDITABLE);
		label.setAlignment(Alignment.Center);
		label.setInsets(fh * 0.25f, 0, 0, 0);
		label.setUserData(this);
		v.addDrawingPrimitive(label);

		if (modelPrimitive != null)
			v.addDrawingPrimitive(modelPrimitive);

	}

}

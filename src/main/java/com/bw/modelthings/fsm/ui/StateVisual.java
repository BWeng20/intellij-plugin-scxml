package com.bw.modelthings.fsm.ui;

import com.bw.graph.Alignment;
import com.bw.graph.DrawContext;
import com.bw.graph.VisualModel;
import com.bw.graph.editor.EditorProxy;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.Line;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.primitive.Rectangle;
import com.bw.graph.primitive.Text;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.InsetsFloat;
import com.bw.graph.visual.ConnectorVisual;
import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.GenericPrimitiveVisual;
import com.bw.graph.visual.VisualFlags;
import com.bw.modelthings.fsm.model.State;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * Visual to represent a FSM State.
 */
public class StateVisual extends GenericPrimitiveVisual
{
	/**
	 * The FSM state of this visual.
	 */
	protected State _state;

	/**
	 * State outer draw context.
	 */
	private DrawContext _stateOuterContext;

	/**
	 * The draw context for inner primitives.
	 */
	private DrawContext _stateInnerContext;

	private StateNameProxy _nameProxy;

	/**
	 * Create a state .
	 *
	 * @param state             The state.
	 * @param textComponent     The text component to use for inplace-edit.
	 * @param stateOuterContext The Drawing context to use for outline.
	 * @param stateInnerContext The Drawing context to use for inner drawings.
	 */
	public StateVisual(State state, JTextComponent textComponent, DrawContext stateOuterContext, DrawContext stateInnerContext)
	{
		super(state._docId, stateOuterContext);
		_state = state;
		this._stateOuterContext = stateOuterContext;
		this._stateInnerContext = stateInnerContext;
		this._nameProxy = new StateNameProxy(textComponent);

	}

	/**
	 * Getsh the FSM state that the visual represents.
	 *
	 * @return The state. Never null.
	 */
	public State getState()
	{
		return _state;
	}

	/**
	 * Creates the primitives.
	 *
	 * @param x           Base X Position
	 * @param y           Base Y Position
	 * @param g2          Graphics to use for dimension calculations.
	 * @param bounds      Outer bounds. If null bounds will be calculated.
	 * @param displayName The name to show. If null the name of the state is used.
	 */
	public void createStatePrimitives(float x, float y, Graphics2D g2,
									  PosAndBounds bounds, String displayName)
	{
		float fh = _stateInnerContext._style._fontMetrics.getHeight();

		ModelPrimitive modelPrimitive = getPrimitiveOf(ModelPrimitive.class);

		if (displayName == null)
			displayName = _state._name;

		removeAllDrawingPrimitives();
		if (bounds == null)
		{
			Rectangle2D stringBounds = _stateInnerContext._style._fontMetrics.getStringBounds(displayName, g2);

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
			bounds = new PosAndBounds(new Point2D.Float(x, y), new Rectangle2D.Float(x, y, width, height));
		}
		setAbsolutePosition(bounds.position, bounds.bounds);

		Rectangle frame = new Rectangle(
				0, 0, bounds.bounds.width, bounds.bounds.height, _stateOuterContext._configuration._stateCornerArcSize, _stateOuterContext._configuration,
				_stateOuterContext._style, VisualFlags.ALWAYS);

		frame.setFill(true);
		addDrawingPrimitive(frame);

		Line separator = new Line(0, fh * 1.5f, bounds.bounds.width, fh * 1.5f
				, _stateInnerContext._configuration, _stateInnerContext._style, VisualFlags.ALWAYS);
		addDrawingPrimitive(separator);

		Text label = new Text(0, 0, displayName,
				_stateInnerContext._configuration, _stateInnerContext._style, VisualFlags.ALWAYS);
		label.setFlags(VisualFlags.EDITABLE);
		label.setAlignment(Alignment.Center);
		label.setInsets(fh * 0.25f, 0, 0, 0);
		label.setUserData(_nameProxy);
		addDrawingPrimitive(label);

		if (modelPrimitive != null)
			addDrawingPrimitive(modelPrimitive);

	}


	@Override
	public String toString()
	{
		return _state._name == null ? "Id:" + _state._docId
									: _state._name;
	}

	/**
	 * Gets the child model of the state.
	 *
	 * @return The child model or null.
	 */
	public VisualModel getChildModel()
	{
		return ModelPrimitive.getChildModel(this);

	}

	/**
	 * Get the current shown name.
	 *
	 * @return The state name.
	 */
	public String getCurrentName()
	{
		return getPrimitiveOf(Text.class).getText();
	}

	/**
	 * Proxy to maintain the state name.
	 */
	protected class StateNameProxy implements EditorProxy
	{
		/**
		 * The editor component.
		 */
		public JTextComponent _textComponent;

		/**
		 * Create a new state proxy.
		 *
		 * @param textComponent The text editor component.
		 */
		public StateNameProxy(JTextComponent textComponent)
		{
			this._textComponent = textComponent;
		}

		@Override
		public String toString()
		{
			return _state._name;
		}

		@Override
		public JComponent getEditor(DrawPrimitive text)
		{
			_textComponent.setText(_state._name);
			return _textComponent;
		}

		@Override
		public void endEdit(DrawPrimitive text, VisualModel model, Graphics2D g2)
		{
			String newName = _textComponent.getText()
										   .trim();
			if (text instanceof Text textPrimitive)
			{
				if (!Objects.equals(newName, textPrimitive.getText()))
				{
					setFlags(VisualFlags.MODIFIED);
					Point2D.Float pt = getAbsolutePosition();
					createStatePrimitives(pt.x, pt.y, g2, null, newName);
					setPreferredDimension(null);
					model.getEdgesAt(StateVisual.this).forEach(e -> placeConnector(e, g2));
				}
			}
		}

		@Override
		public void cancelEdit(DrawPrimitive text)
		{
		}
	}

	/**
	 * Places a connector visual that is attached to this state visual.
	 * @param edgeVisual The edge
	 * @param g2 Graphics context, may be null to use cached dimensions.
	 */
	public void placeConnector(EdgeVisual edgeVisual, Graphics2D g2)
	{
		Rectangle2D.Float myBounds = getAbsoluteBounds2D(g2);
		ConnectorVisual sourceConnector = edgeVisual.getSourceConnector();
		if (sourceConnector != null && sourceConnector.getParent() == this)
		{
			sourceConnector.setRelativePosition(myBounds.width, myBounds.height / 2f);
		}
		else
		{
			ConnectorVisual targetConnector = edgeVisual.getTargetConnector();
			if (targetConnector != null && targetConnector.getParent() == this)
			{

				if (targetConnector.getTargetedParentChild() == null)
					targetConnector.setRelativePosition(0, myBounds.height / 2f);
				else
					targetConnector.setRelativePosition(getConfiguration()._innerModelBoxInsets._left,
							myBounds.height / 2f);
			}
		}
	}
}

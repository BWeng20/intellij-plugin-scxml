package com.bw.modelthings.fsm.ui;

import com.bw.graph.Alignment;
import com.bw.graph.DrawContext;
import com.bw.graph.VisualModel;
import com.bw.graph.editor.EditorProxy;
import com.bw.graph.editor.action.EditAction;
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
import com.bw.modelthings.fsm.ui.actions.RenameStateAction;

import javax.swing.JComponent;
import javax.swing.text.JTextComponent;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Visual to represent a FSM State.
 */
public class StateVisual extends GenericPrimitiveVisual
{
	/**
	 * Minimal width of states in pixel.
	 */
	public float _stateMinimalWidth = 50;

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

	private Rectangle _connectorFrame;

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
		{
			displayName = _state._name;
		}
		this._displayName = displayName;

		float d = _state._isFinal ? 5 : 0;

		removeAllDrawingPrimitives();
		if (bounds == null)
		{
			Rectangle2D stringBounds = _stateInnerContext._style._fontMetrics.getStringBounds(displayName, g2);

			float height = 5 * fh;
			float width = (float) Math.max(stringBounds.getWidth() + 10,
					((FsmGraphConfiguration) _stateInnerContext._configuration)._stateMinimalWidth);

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
			bounds = new PosAndBounds(new Point2D.Float(x, y), new Rectangle2D.Float(x, y, 2 * d + width, 2 * d + height));
		}
		setAbsolutePosition(bounds.position, bounds.bounds);

		_connectorFrame = new Rectangle(
				0, 0, bounds.bounds.width, bounds.bounds.height, _stateOuterContext._configuration._stateCornerArcSize, _stateOuterContext._configuration,
				_stateOuterContext._style, VisualFlags.ALWAYS);

		_connectorFrame.setFill(true);
		addDrawingPrimitive(_connectorFrame);

		float px = d;
		float py = d;
		float ph = bounds.bounds.height - 2 * d;

		if (_state._isFinal)
		{
			Rectangle innerFrame = new Rectangle(
					px, py, bounds.bounds.width - 2 * d, ph,
					_stateOuterContext._configuration._stateCornerArcSize - d, _stateOuterContext._configuration,
					_stateOuterContext._style, VisualFlags.ALWAYS);
			addDrawingPrimitive(innerFrame);
		}

		Text label = new Text(0, py, displayName,
				_stateInnerContext._configuration, _stateInnerContext._style, VisualFlags.ALWAYS);
		label.setFlags(VisualFlags.EDITABLE);
		label.setAlignment(Alignment.Center);
		label.setInsets(fh * 0.25f, 0, 0, 0);
		label.setUserData(_nameProxy);
		addDrawingPrimitive(label);

		Line separator = new Line(px, py + fh * 1.5f, px + bounds.bounds.width - 2 * d, py + fh * 1.5f
				, _stateInnerContext._configuration, _stateInnerContext._style, VisualFlags.ALWAYS);
		addDrawingPrimitive(separator);

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
		Text txt = getPrimitiveOf(Text.class);
		return txt == null ? _displayName : txt.getText();
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

		/**
		 * Commits the edited text, updates the text-primitive and the Layout of the StateVisual.
		 *
		 * @param text  The primitive to update.
		 * @param model The model.
		 * @param g2    Graphics context for calculations.
		 */
		@Override
		public EditAction endEdit(DrawPrimitive text, VisualModel model, Graphics2D g2)
		{
			String newName = _textComponent.getText()
										   .trim();
			if (text instanceof Text textPrimitive)
			{
				String oldName = textPrimitive.getText();
				if (!Objects.equals(newName, oldName))
				{
					setFlags(VisualFlags.MODIFIED);
					Point2D.Float pt = getAbsolutePosition();
					createStatePrimitives(pt.x, pt.y, g2, null, newName);
					setPreferredDimension(null);
					placeConnectors(model.getEdgesAt(StateVisual.this), g2);

					return new RenameStateAction(oldName, newName);
				}
			}
			return null;
		}

		@Override
		public void cancelEdit(DrawPrimitive text)
		{
		}
	}

	private float getOtherVisualAbsoluteY(EdgeVisual ev)
	{
		if (ev.getSourceVisual() == StateVisual.this)
		{
			return (float) ev.getTargetVisuals().stream().collect(Collectors.summarizingDouble(cv -> cv.getAbsolutePosition().y)).getAverage();
		}
		else
		{
			return ev.getSourceVisual().getAbsolutePosition().y;
		}
	}

	/**
	 * Places the connectors that are attached to this state visual.
	 *
	 * @param edgeVisuals The edges
	 * @param g2          Graphics context, may be null to use cached dimensions.
	 */
	public void placeConnectors(List<EdgeVisual> edgeVisuals, Graphics2D g2)
	{
		edgeVisuals.sort((e1, e2) ->
		{
			float y1 = getOtherVisualAbsoluteY(e1);
			float y2 = getOtherVisualAbsoluteY(e2);
			return ((y1 - y2) < 0 ? -1 : 0);
		});

		ArrayList<ConnectorVisual> asSource = new ArrayList<>();
		ArrayList<ConnectorVisual> asTarget = new ArrayList<>();
		for (EdgeVisual edgeVisual : edgeVisuals)
		{
			ConnectorVisual sourceConnector = edgeVisual.getSourceConnector();
			if (sourceConnector != null && sourceConnector.getParent() == this && sourceConnector.getRelativePosition() == null)
			{
				asSource.add(sourceConnector);
			}
			else
			{
				for (ConnectorVisual targetConnector : edgeVisual.getTargetConnectors())
				{
					if (targetConnector.getParent() == this && targetConnector.getRelativePosition() == null)
					{
						asTarget.add(targetConnector);
					}
				}
			}
		}

		// Distribute connectors
		Rectangle2D.Float myBounds = getAbsoluteBounds2D(g2);

		float yd = myBounds.height / asSource.size();
		float y = yd / 2f;
		for (ConnectorVisual sourceConnector : asSource)
		{
			sourceConnector.setRelativePosition(myBounds.width, y);
			y += yd;
		}
		yd = myBounds.height / asTarget.size();
		y = yd / 2f;
		for (ConnectorVisual targetConnector : asTarget)
		{
			if (targetConnector.getTargetedParentChild() == null)
				targetConnector.setRelativePosition(0, y);
			else
				targetConnector.setRelativePosition(getConfiguration()._innerModelBoxInsets._left, y);
			y += yd;
		}
	}

	@Override
	public Shape getConnectorShape()
	{
		return _connectorFrame.getShape();
	}
}

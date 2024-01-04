package com.bw.modeldrive.editor;

import com.bw.graph.*;
import com.bw.modeldrive.model.FiniteStateMachine;
import com.bw.modeldrive.model.State;
import com.intellij.openapi.Disposable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.*;
import java.awt.geom.Rectangle2D;

/**
 * Panel to show the FSM as Graphical State Machine.
 */
public class ScxmlGraphPanel extends JPanel implements Disposable
{
	/**
	 * Dummy to show something
	 */
	protected JLabel info;

	/**
	 * The graph pane.
	 */
	protected GraphPane pane = new GraphPane();

	/**
	 * Style for state outline.
	 */
	protected DrawStyle stateOutlineStyle = new DrawStyle();

	/**
	 * Style for start nodes.
	 */
	protected DrawStyle startStyle = new DrawStyle();

	/**
	 * Style for state outline high-lighted.
	 */
	protected DrawStyle stateOutlineStyleHighlight = new DrawStyle();

	/**
	 * Style for state text.
	 */
	protected DrawStyle stateTextStyle = new DrawStyle();


	/**
	 * Context for state outline.
	 */
	protected DrawContext stateOutlineContext = new DrawContext(stateOutlineStyle, stateOutlineStyleHighlight);

	@Override
	public void dispose()
	{
		pane.dispose();
	}

	/**
	 * Create a new editor panel.
	 */
	public ScxmlGraphPanel()
	{
		super(new BorderLayout());
		info = new JLabel("SCXML");
		add(info, BorderLayout.NORTH);
		add(new JScrollPane(pane), BorderLayout.CENTER);

		Font font = getFont();
		if (font == null)
			font = new Font(Font.DIALOG, Font.PLAIN, 12);

		FontMetrics fontMetrics = getFontMetrics(font);

		// @TODO: Make styles configurable.

		startStyle.linePaint = Color.BLACK;
		startStyle.fillPaint = Color.BLACK;

		stateOutlineStyle.linePaint = Color.BLACK;
		stateOutlineStyle.fillPaint = Color.GRAY;
		stateOutlineStyle.lineStroke = new BasicStroke(2);
		stateOutlineStyle.textPaint = getForeground();
		stateOutlineStyle.font = font;
		stateOutlineStyle.fontMetrics = fontMetrics;

		stateOutlineStyleHighlight.linePaint = Color.RED;
		stateOutlineStyleHighlight.lineStroke = new BasicStroke(4);
		stateOutlineStyleHighlight.textPaint = getForeground();
		stateOutlineStyleHighlight.font = font;
		stateOutlineStyleHighlight.fontMetrics = fontMetrics;

		stateTextStyle.linePaint = stateOutlineStyle.linePaint;
		stateTextStyle.lineStroke = new BasicStroke(4);
		stateTextStyle.textPaint = getForeground();
		stateTextStyle.orientation = Orientation.Center;
		stateTextStyle.font = font;
		stateTextStyle.fontMetrics = fontMetrics;
	}

	/**
	 * Sets the FSM to show.
	 *
	 * @param fsm The FSM to show.
	 */
	public void setStateMachine(FiniteStateMachine fsm)
	{
		pane.removeAllVisuals();

		if (fsm != null && fsm.pseudoRoot != null)
		{
			final float grapY = 5;

			Graphics2D g2 = (Graphics2D) pane.getGraphics();

			int fh = stateOutlineStyle.fontMetrics.getHeight();

			int x = fh;
			int y = fh;

			Visual startNode = createStartVisual(x + fh, y + fh, fh);
			pane.addVisual(startNode);

			x += 2 * fh;

			for (State initalState : fsm.pseudoRoot.states)
			{
				Visual stateVisual = createStateVisual(x, y, initalState, g2);
				pane.addVisual(stateVisual);
				stateVisual.updateBounds(g2);
				y += stateVisual.getBounds2D().height + grapY;
			}
		}
	}

	/**
	 * Creates a visual for a start-node
	 *
	 * @param x Base X Position
	 * @param y Base Y Position
	 * @return The visual
	 */
	protected Visual createStartVisual(float x, float y, float size)
	{
		Visual startNode = new Visual(stateOutlineContext);
		CirclePrimitive circle = new CirclePrimitive(x, y, startStyle, false, size);
		circle.setFill(true);
		startNode.addDrawingPrimitive(circle);
		return startNode;
	}

	/**
	 * Creates a visual for a state
	 *
	 * @param x     Base X Position
	 * @param y     Base Y Position
	 * @param state State to create the visual for.
	 * @param g2    Graphics to use for dimension calculations.
	 * @return The creates visual.
	 */
	protected Visual createStateVisual(float x, float y, State state, Graphics2D g2)
	{
		Visual v = new Visual(stateOutlineContext);
		v.setPosition(x, y);

		TextPrimitive label = new TextPrimitive(3, 3,
				stateTextStyle, false, state.name);

		Rectangle2D stringBounds = stateTextStyle.fontMetrics.getStringBounds(state.name, g2);
		float height = 5 * stateTextStyle.fontMetrics.getHeight();

		RectanglePrimitive frame = new RectanglePrimitive(
				0, 0,
				stateOutlineStyle, false,
				(float) (stringBounds.getWidth() + 6), height);
		frame.setFill(true);

		v.addDrawingPrimitive(frame);
		v.addDrawingPrimitive(label);

		return v;
	}

	/**
	 * In case the FSM could not be loaded, show the cause.
	 *
	 * @param cause The error cause, can be null.
	 */
	public void setError(Throwable cause)
	{
		info.setText(cause == null ? "---" : cause.getLocalizedMessage());
	}
}

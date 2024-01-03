package com.bw.modeldrive.editor;

import com.bw.graph.*;
import com.bw.modeldrive.model.FiniteStateMachine;
import com.intellij.openapi.Disposable;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.geom.Point2D;

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
	 * Style for state outline high-lighted.
	 */
	protected DrawStyle stateOutlineStyleHighlight = new DrawStyle();

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

		Visual v = new Visual(stateOutlineContext);
		pane.addVisual(v);

		stateOutlineStyle.linePaint = Color.BLACK;
		stateOutlineStyle.lineStroke = new BasicStroke(2);
		stateOutlineStyle.font = Font.getFont(Font.DIALOG);
		stateOutlineStyle.textPaint = Color.BLUE;

		stateOutlineStyleHighlight.linePaint = Color.RED;
		stateOutlineStyleHighlight.lineStroke = new BasicStroke(4);
		stateOutlineStyleHighlight.font = Font.getFont(Font.DIALOG);
		stateOutlineStyleHighlight.textPaint = Color.BLUE;

		v.addDrawingPrimitive(new RectanglePrimitive(new Point2D.Float(0, 0),
				null, false, 20, 30));

		v.setPosition(5, 5);
	}

	/**
	 * Sets the FSM to show.
	 *
	 * @param fsm The FSM to show.
	 */
	public void setStateMachine(FiniteStateMachine fsm)
	{
		// @TODO
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

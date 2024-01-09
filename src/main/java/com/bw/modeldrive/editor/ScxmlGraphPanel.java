package com.bw.modeldrive.editor;

import com.bw.graph.Alignment;
import com.bw.graph.CirclePrimitive;
import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphPane;
import com.bw.graph.LinePrimitive;
import com.bw.graph.RectanglePrimitive;
import com.bw.graph.TextPrimitive;
import com.bw.graph.Visual;
import com.bw.modeldrive.model.FiniteStateMachine;
import com.bw.modeldrive.model.State;
import com.bw.modeldrive.settings.ChangeConfigurationNotifier;
import com.bw.modeldrive.settings.Configuration;
import com.bw.modeldrive.settings.PersistenceService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.util.messages.MessageBusConnection;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
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
	 * Style for inner lines.
	 */
	protected DrawStyle stateInlineStyle = new DrawStyle();

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
	protected DrawContext stateOutlineContext = new DrawContext(pane.getGraphConfiguration(), stateOutlineStyle, stateOutlineStyleHighlight);

	/**
	 * The project of the file.
	 */
	protected Project theProject;

	/**
	 * Message bus for change notification.
	 */
	private MessageBusConnection mbCon;


	@Override
	public void dispose()
	{
		mbCon.dispose();
		pane.dispose();
	}

	/**
	 * Sets the configuration.
	 *
	 * @param config The config to use.
	 */
	protected void setConfiguration(Configuration config)
	{
		if (config != null)
		{
			var graphConfig = pane.getGraphConfiguration();
			if (graphConfig.doubleBuffered != config.doublebuffered ||
					graphConfig.antialiasing != config.antialiasing ||
					graphConfig.zoomByMetaMouseWheelEnabled != config.zoomByMetaMouseWheelEnabled
			)
			{
				graphConfig.doubleBuffered = config.doublebuffered;
				graphConfig.antialiasing = config.antialiasing;
				graphConfig.zoomByMetaMouseWheelEnabled = config.zoomByMetaMouseWheelEnabled;
				SwingUtilities.invokeLater(() -> {
					pane.invalidate();
					pane.repaint();
				});
			}
		}
	}

	/**
	 * Create a new editor panel.
	 *
	 * @param theProject The project of the file to show.
	 */
	public ScxmlGraphPanel(Project theProject)
	{
		super(new BorderLayout());
		this.theProject = theProject;
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

		stateInlineStyle.linePaint = stateOutlineStyle.linePaint;
		stateInlineStyle.fillPaint = stateOutlineStyle.fillPaint;
		stateInlineStyle.lineStroke = new BasicStroke(1);
		stateInlineStyle.textPaint = stateOutlineStyle.textPaint;
		stateInlineStyle.font = font;
		stateInlineStyle.fontMetrics = fontMetrics;

		stateOutlineStyleHighlight.linePaint = Color.RED;
		stateOutlineStyleHighlight.lineStroke = new BasicStroke(2);
		stateOutlineStyleHighlight.textPaint = getForeground();
		stateOutlineStyleHighlight.font = font;
		stateOutlineStyleHighlight.fontMetrics = fontMetrics;

		stateTextStyle.linePaint = stateOutlineStyle.linePaint;
		stateTextStyle.lineStroke = new BasicStroke(4);
		stateTextStyle.textPaint = getForeground();
		stateTextStyle.alignment = Alignment.Center;
		stateTextStyle.font = font;
		stateTextStyle.fontMetrics = fontMetrics;

		mbCon = theProject.getMessageBus().connect();
		mbCon.subscribe(ChangeConfigurationNotifier.CHANGE_CONFIG_TOPIC, (ChangeConfigurationNotifier) this::setConfiguration);

		PersistenceService persistenceService = theProject.getService(PersistenceService.class);
		if (persistenceService != null)
		{
			setConfiguration(persistenceService.getState());
		}
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

			float fh = stateOutlineStyle.fontMetrics.getHeight();

			float x = fh;
			float y = fh;

			Visual startNode = createStartVisual(x + fh / 2, y + fh, fh / 2);
			pane.addVisual(startNode);

			x += 2 * fh;

			for (State initalState : fsm.pseudoRoot.states)
			{
				Visual stateVisual = createStateVisual(x, y, initalState, g2);
				pane.addVisual(stateVisual);
				y += stateVisual.getBounds2D(g2).height + grapY;
			}
		}
	}

	/**
	 * Creates a visual for a start-node
	 *
	 * @param x      Base X Position
	 * @param y      Base Y Position
	 * @param radius Radius of circle.
	 * @return The visual
	 */
	protected Visual createStartVisual(float x, float y, float radius)
	{
		Visual startNode = new Visual(stateOutlineContext);
		CirclePrimitive circle = new CirclePrimitive(radius, radius, pane.getGraphConfiguration(), startStyle, false, radius);
		circle.setFill(true);
		startNode.setPosition(x, y);
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

		Rectangle2D stringBounds = stateTextStyle.fontMetrics.getStringBounds(state.name, g2);
		float fh = stateTextStyle.fontMetrics.getHeight();
		float height = 5 * fh;

		RectanglePrimitive frame = new RectanglePrimitive(
				0, 0, pane.getGraphConfiguration(),
				stateOutlineStyle, false,
				(float) (stringBounds.getWidth() + 10), height);
		frame.setFill(true);
		v.addDrawingPrimitive(frame);

		LinePrimitive separator = new LinePrimitive(0, fh * 1.5f, (float) (stringBounds.getWidth() + 10), fh * 1.5f
				, pane.getGraphConfiguration(), stateInlineStyle);
		v.addDrawingPrimitive(separator);

		TextPrimitive label = new TextPrimitive(0, 0, pane.getGraphConfiguration(),
				stateTextStyle, false, state.name);
		label.setInsets(fh * 0.25f, 0, 0, 0);

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

package com.bw.modeldrive.intellij.editor;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphPane;
import com.bw.modeldrive.fsm.model.FiniteStateMachine;
import com.bw.modeldrive.fsm.ui.GraphFactory;
import com.bw.modeldrive.intellij.settings.ChangeConfigurationNotifier;
import com.bw.modeldrive.intellij.settings.Configuration;
import com.bw.modeldrive.intellij.settings.PersistenceService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.diagnostic.Logger;
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
	 * Style for inner drawings in the states.
	 */
	protected DrawStyle stateInnerStyle = new DrawStyle();

	/**
	 * Style for start nodes.
	 */
	protected DrawStyle startStyle = new DrawStyle();

	/**
	 * Style for state outline high-lighted.
	 */
	protected DrawStyle stateOutlineStyleHighlight = new DrawStyle();

	/**
	 * Context for state outline.
	 */
	protected DrawContext stateOutlineContext = new DrawContext(pane.getGraphConfiguration(), stateOutlineStyle, stateOutlineStyleHighlight);

	/**
	 * The context for inner drawing for states.
	 */
	protected DrawContext stateInnerContext = new DrawContext(pane.getGraphConfiguration(),
			stateInnerStyle, stateInnerStyle);


	/**
	 * Context for edges.
	 */
	protected DrawContext edgeContext = new DrawContext(pane.getGraphConfiguration(), stateInnerStyle, stateInnerStyle);


	/**
	 * Context for start node.
	 */
	protected DrawContext startContext = new DrawContext(pane.getGraphConfiguration(), startStyle, startStyle);

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

		stateInnerStyle.linePaint = stateOutlineStyle.linePaint;
		stateInnerStyle.fillPaint = stateOutlineStyle.fillPaint;
		stateInnerStyle.lineStroke = new BasicStroke(1);
		stateInnerStyle.textPaint = stateOutlineStyle.textPaint;
		stateInnerStyle.font = font;
		stateInnerStyle.fontMetrics = fontMetrics;

		stateOutlineStyleHighlight.linePaint = Color.RED;
		stateOutlineStyleHighlight.lineStroke = new BasicStroke(2);
		stateOutlineStyleHighlight.textPaint = getForeground();
		stateOutlineStyleHighlight.font = font;
		stateOutlineStyleHighlight.fontMetrics = fontMetrics;

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
		GraphFactory factory = new GraphFactory();

		pane.setModel(factory.createVisualModel(fsm, (Graphics2D) pane.getGraphics(),
				startContext, stateOutlineContext, stateInnerContext, edgeContext));

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

package com.bw.modeldrive.intellij.editor;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.VisualModel;
import com.bw.graph.editor.GraphPane;
import com.bw.graph.editor.InteractionListener;
import com.bw.graph.visual.Visual;
import com.bw.modeldrive.fsm.model.FiniteStateMachine;
import com.bw.modeldrive.fsm.ui.GraphFactory;
import com.bw.modeldrive.intellij.settings.ChangeConfigurationNotifier;
import com.bw.modeldrive.intellij.settings.Configuration;
import com.bw.modeldrive.intellij.settings.PersistenceService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.breadcrumbs.Breadcrumbs;
import com.intellij.ui.components.breadcrumbs.Crumb;
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
import java.util.ArrayList;
import java.util.List;

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
	 * Breadcrumbs to show and select the parent states of the current model.
	 */
	protected Breadcrumbs stateHierarchyBreadCrumbs;

	/**
	 * Array to add the state crumbs for {@link #stateHierarchyBreadCrumbs}.
	 */
	protected List<Crumb> stateHierarchyCrumbs = new ArrayList<>();

	/**
	 * Root visual.
	 */
	protected Visual root;

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
	 * Style for highlighted edges.
	 */
	protected DrawStyle edgeHighlightStyle = new DrawStyle();

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
	protected DrawContext edgeContext = new DrawContext(pane.getGraphConfiguration(), stateInnerStyle, edgeHighlightStyle);


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
	 * Breadcrumbs implementation to show the State hierarchy.
	 */
	protected static class StateBreadCrumbs extends Breadcrumbs
	{
		/**
		 * Create a new state-breadcrumbs component.
		 */
		public StateBreadCrumbs()
		{
		}

		/**
		 * Same as in com.intellij.xml.breadcrumbs.PsiBreadcrumbs
		 */
		private TextAttributesKey getKey(Crumb crumb)
		{
			if (this.isHovered(crumb))
			{
				return EditorColors.BREADCRUMBS_HOVERED;
			}
			else if (this.isSelected(crumb))
			{
				return EditorColors.BREADCRUMBS_CURRENT;
			}
			else
			{
				return this.isAfterSelected(crumb) ? EditorColors.BREADCRUMBS_INACTIVE : EditorColors.BREADCRUMBS_DEFAULT;
			}
		}

		/**
		 * Same as in com.intellij.xml.breadcrumbs.PsiBreadcrumbs
		 */
		protected TextAttributes getAttributes(Crumb crumb)
		{
			TextAttributesKey key = this.getKey(crumb);
			return key == null ? null : EditorColorsManager.getInstance().getGlobalScheme().getAttributes(key);
		}
	}

	/**
	 * Crumb implementation to show state hierarchy.
	 *
	 * @see StateBreadCrumbs
	 */
	protected static class StateCrumb implements Crumb
	{
		Visual state;

		@Override
		public String getText()
		{
			return String.valueOf(state.getId());
		}

		/**
		 * Creates a new crumb for the given state.
		 *
		 * @param state The references parent state.
		 */
		public StateCrumb(Visual state)
		{
			this.state = state;
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

		stateHierarchyBreadCrumbs = new StateBreadCrumbs();
		add(stateHierarchyBreadCrumbs, BorderLayout.SOUTH);

		stateHierarchyBreadCrumbs.onHover((crumb, inputEvent) -> {
		});

		stateHierarchyBreadCrumbs.onSelect((crumb, inputEvent) -> {
			StateCrumb sc = (StateCrumb) crumb;
			if (sc != null)
				pane.setModel(sc.state.getInnerModel());
		});

		// add(bc, BorderLayout.NORTH);
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

		edgeHighlightStyle.linePaint = Color.RED;
		edgeHighlightStyle.fillPaint = stateOutlineStyle.fillPaint;
		edgeHighlightStyle.lineStroke = new BasicStroke(2);
		edgeHighlightStyle.textPaint = stateOutlineStyle.textPaint;
		edgeHighlightStyle.font = font;
		edgeHighlightStyle.fontMetrics = fontMetrics;

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

		pane.addInteractionListener(new InteractionListener()
		{
			@Override
			public void selected(Visual visual)
			{
			}

			@Override
			public void deselected(Visual visual)
			{
			}

			@Override
			public void hierarchyChanged()
			{
				updatedStateBreadcrumbs();
			}
		});
	}

	/**
	 * Updates the state hierarchy breadcrumbs.
	 */
	protected void updatedStateBreadcrumbs()
	{
		stateHierarchyCrumbs.clear();
		if (root != null)
		{
			stateHierarchyCrumbs.add(new StateCrumb(root));
		}
		for (Visual v : pane.getHierarchy())
		{
			stateHierarchyCrumbs.add(new StateCrumb(v));
		}
		stateHierarchyBreadCrumbs.setCrumbs(stateHierarchyCrumbs);
	}

	/**
	 * Sets the FSM to show.
	 *
	 * @param fsm The FSM to show.
	 */
	public void setStateMachine(FiniteStateMachine fsm)
	{
		GraphFactory factory = new GraphFactory();

		pane.setModel(null);
		if (root != null)
		{
			root.dispose();
			root = null;
		}
		VisualModel rootModel =
				factory.createVisualModel(fsm, (Graphics2D) pane.getGraphics(),
						startContext, stateOutlineContext, stateInnerContext, edgeContext);

		if (!rootModel.getVisuals().isEmpty())
		{
			root = rootModel.getVisuals().get(0);
			pane.setModel(root.getInnerModel());
		}
		else
		{
			root = null;
			pane.setModel(null);
		}
		updatedStateBreadcrumbs();
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

package com.bw.modelthings.intellij.editor;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.VisualModel;
import com.bw.graph.editor.GraphPane;
import com.bw.graph.editor.InteractionAdapter;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.visual.Visual;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.ui.GraphExtension;
import com.bw.modelthings.fsm.ui.GraphFactory;
import com.bw.modelthings.fsm.ui.StateNameProxy;
import com.bw.modelthings.intellij.settings.Configuration;
import com.bw.modelthings.intellij.settings.PersistenceService;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.editor.HighlighterColors;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.breadcrumbs.Breadcrumbs;
import com.intellij.ui.components.breadcrumbs.Crumb;

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
	 * The in-place editor for state-names.
	 */
	protected JBTextField stateNameEditorComponent = new JBTextField();

	/**
	 * Breadcrumbs to show and select the parent states of the current model.
	 */
	protected Breadcrumbs stateHierarchyBreadCrumbs;

	/**
	 * Array to add the state crumbs for {@link #stateHierarchyBreadCrumbs}.
	 */
	protected List<Crumb> stateHierarchyCrumbs = new ArrayList<>();

	/**
	 * Root model.
	 */
	protected Visual root;

	/**
	 * The graph pane.
	 */
	protected GraphPane pane = new GraphPane();

	/**
	 * The FSM.
	 */
	protected FiniteStateMachine fsm;

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
	 * Context for state outline.
	 */
	protected DrawContext stateOutlineContext = new DrawContext(pane.getGraphConfiguration(), stateOutlineStyle);

	/**
	 * The context for inner drawing for states.
	 */
	protected DrawContext stateInnerContext = new DrawContext(pane.getGraphConfiguration(), stateInnerStyle);

	/**
	 * Context for edges.
	 */
	protected DrawContext edgeContext = new DrawContext(pane.getGraphConfiguration(), stateInnerStyle);

	/**
	 * Context for start node.
	 */
	protected DrawContext startContext = new DrawContext(pane.getGraphConfiguration(), startStyle);

	/**
	 * The project of the file.
	 */
	protected Project theProject;

	@Override
	public void dispose()
	{
		GraphLafManagerListener.removeGraphLafListener(lafListener);
		if (fsm != null)
		{
			fsm.dispose();
			fsm = null;
		}
		pane.dispose();
	}

	/**
	 * Sets the configuration.
	 *
	 * @param config The config to use.
	 */
	public void setConfiguration(Configuration config)
	{
		if (config != null)
		{
			var graphConfig = pane.getGraphConfiguration();
			if (graphConfig.buffered != config.buffered ||
					graphConfig.antialiasing != config.antialiasing ||
					graphConfig.zoomByMetaMouseWheelEnabled != config.zoomByMetaMouseWheelEnabled
			)
			{
				graphConfig.buffered = config.buffered;
				graphConfig.antialiasing = config.antialiasing;
				graphConfig.zoomByMetaMouseWheelEnabled = config.zoomByMetaMouseWheelEnabled;
				SwingUtilities.invokeLater(() ->
				{
					pane.invalidate();
					pane.repaint();
				});
			}
		}
	}

	/**
	 * Get the SVG for the current model.
	 *
	 * @return The SVG source code.
	 */
	public String getSVG()
	{
		return pane.toSVG();
	}

	/**
	 * Get the state of the currently selected visual.
	 *
	 * @return The state or null.
	 */
	public State getSelectedState()
	{
		Visual v = pane.getSelectedVisual();
		if (v != null)
		{
			StateNameProxy stateProxy = v.getProxyOf(StateNameProxy.class);
			if (stateProxy != null)
			{
				return stateProxy.state;
			}
		}
		return null;
	}

	/**
	 * Remove a state.
	 *
	 * @param state           The state.
	 * @param keepChildStates If true child-states and internal transitions are moved to parent-state.
	 */
	public void removeState(State state, boolean keepChildStates)
	{
		if (fsm != null)
		{
			List<State> removedStates = fsm.remove(state, keepChildStates);
		}
	}

	/**
	 * Extracts the name of the state behind the visual.
	 *
	 * @param stateVisual The state visual.
	 * @return The name or null - if the visual is no state or if the name is not set.
	 */
	public String getNameOfState(Visual stateVisual)
	{
		if (stateVisual != null)
		{
			StateNameProxy stateProxy = stateVisual.getProxyOf(StateNameProxy.class);
			if (stateProxy != null)
			{
				return stateProxy.state.name;
			}
		}
		return null;
	}

	/**
	 * Get the graph configuration.
	 *
	 * @return The graph configuration.
	 */
	public GraphConfiguration getGraphConfiguration()
	{
		return pane.getGraphConfiguration();
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
			return key == null ? null : EditorColorsManager.getInstance()
														   .getGlobalScheme()
														   .getAttributes(key);
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
			return state.getDisplayName();
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

		stateHierarchyBreadCrumbs.onHover((crumb, inputEvent) ->
		{
		});

		stateHierarchyBreadCrumbs.onSelect((crumb, inputEvent) ->
		{
			StateCrumb sc = (StateCrumb) crumb;
			if (sc != null)
				pane.setModel(ModelPrimitive.getChildModel(sc.state));
		});

		// add(bc, BorderLayout.NORTH);
		add(new JScrollPane(pane), BorderLayout.CENTER);

		updateLaf();

		PersistenceService persistenceService = theProject.getService(PersistenceService.class);
		if (persistenceService != null)
		{
			setConfiguration(persistenceService.getState());
		}

		pane.addInteractionListener(new InteractionAdapter()
		{
			@Override
			public void hierarchyChanged()
			{
				updatedStateBreadcrumbs();
			}

		});

		GraphLafManagerListener.addGraphLafListener(lafListener);
	}

	/**
	 * The Laf listener.
	 */
	protected LafListener lafListener = new LafListener();

	/**
	 * Updates graphical settings from current LAF.
	 * This method doesn't call "repaint" itself.
	 */
	protected void updateLaf()
	{
		// @TODO: Make styles configurable.

		TextAttributes textAttribute = EditorColorsManager.getInstance()
														  .getSchemeForCurrentUITheme()
														  .getAttributes(HighlighterColors.TEXT);
		Color background = textAttribute.getBackgroundColor();

		pane.setOpaque(true);
		pane.getGraphConfiguration().graphBackground = background;
		pane.setBackground(background);

		stateHierarchyBreadCrumbs.setBackground(background);

		Font font = getFont();
		if (font == null)
			font = new Font(Font.DIALOG, Font.PLAIN, 12);

		FontMetrics fontMetrics = getFontMetrics(font);

		startStyle.linePaint = textAttribute.getForegroundColor();
		startStyle.fillPaint = textAttribute.getForegroundColor();

		stateOutlineStyle.linePaint = textAttribute.getForegroundColor();
		stateOutlineStyle.fillPaint = textAttribute.getBackgroundColor();
		stateOutlineStyle.lineStroke = new BasicStroke(2);
		stateOutlineStyle.textPaint = textAttribute.getForegroundColor();

		stateOutlineStyle.font = font;
		stateOutlineStyle.fontMetrics = fontMetrics;

		stateInnerStyle.linePaint = stateOutlineStyle.linePaint;
		stateInnerStyle.fillPaint = stateOutlineStyle.fillPaint;
		stateInnerStyle.lineStroke = new BasicStroke(1);
		stateInnerStyle.textPaint = stateOutlineStyle.textPaint;
		stateInnerStyle.background = background;
		stateInnerStyle.font = font;
		stateInnerStyle.fontMetrics = fontMetrics;

		pane.getModel()
			.repaint();
	}

	/**
	 * Laf Listener implementation.
	 *
	 * @see GraphLafManagerListener
	 */
	protected class LafListener implements GraphLafListener
	{
		/**
		 * Create a new instance of the listener.
		 */
		LafListener()
		{

		}

		@Override
		public void lafChanged()
		{
			SwingUtilities.invokeLater(() ->
			{
				updateLaf();
				repaint();
			});
		}
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
	 * @param fsm            The FSM to show.
	 * @param graphExtension The graph-extension or null
	 */
	public void setStateMachine(FiniteStateMachine fsm, GraphExtension graphExtension)
	{
		GraphFactory factory = new GraphFactory(graphExtension);
		factory.setStateNameEditorComponent(stateNameEditorComponent);

		pane.setModel(null);
		if (root != null)
		{
			root.dispose();
			root = null;
		}
		this.fsm = fsm;
		VisualModel rootModel =
				factory.createVisualModel(fsm, (Graphics2D) pane.getGraphics(),
						startContext, stateOutlineContext, stateInnerContext, edgeContext);

		if (!rootModel.getVisuals()
					  .isEmpty())
		{
			root = rootModel.getVisuals()
							.get(0);
			pane.setModel(ModelPrimitive.getChildModel(root));
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

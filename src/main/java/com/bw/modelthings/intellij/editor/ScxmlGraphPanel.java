package com.bw.modelthings.intellij.editor;

import com.bw.graph.editor.InteractionAdapter;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.visual.Visual;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.ui.FsmGraphPanel;
import com.bw.modelthings.fsm.ui.ScxmlGraphExtension;
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
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel to show the FSM as Graphical State Machine.
 */
public class ScxmlGraphPanel extends FsmGraphPanel implements Disposable
{
	/**
	 * Dummy to show something
	 */
	protected JLabel _info;

	/**
	 * Breadcrumbs to show and select the parent states of the current model.
	 */
	protected Breadcrumbs _stateHierarchyBreadCrumbs;

	/**
	 * Array to add the state crumbs for {@link #_stateHierarchyBreadCrumbs}.
	 */
	protected List<Crumb> _stateHierarchyCrumbs = new ArrayList<>();

	/**
	 * The project of the file.
	 */
	protected Project _theProject;

	@Override
	public void dispose()
	{
		GraphLafManagerListener.removeGraphLafListener(_lafListener);
		super.dispose();
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
			var graphConfig = _pane.getGraphConfiguration();
			if (graphConfig._buffered != config._buffered ||
					graphConfig._antialiasing != config._antialiasing ||
					graphConfig._zoomByMetaMouseWheelEnabled != config._zoomByMetaMouseWheelEnabled
			)
			{
				graphConfig._buffered = config._buffered;
				graphConfig._antialiasing = config._antialiasing;
				graphConfig._zoomByMetaMouseWheelEnabled = config._zoomByMetaMouseWheelEnabled;
				SwingUtilities.invokeLater(() ->
				{
					_pane.invalidate();
					_pane.repaint();
				});
			}
		}
	}


	/**
	 * Remove a state.
	 *
	 * @param state           The state.
	 * @param keepChildStates If true child-states and internal transitions are moved to parent-state.
	 */
	@Override
	public void removeState(State state, boolean keepChildStates)
	{
		super.removeState(state, keepChildStates);
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
		// Reconfigure the in-place editor to use JBTextField
		_stateNameEditorComponent = new JBTextField();

		this._theProject = theProject;
		_info = new JLabel("SCXML");

		_stateHierarchyBreadCrumbs = new StateBreadCrumbs();
		add(_stateHierarchyBreadCrumbs, BorderLayout.SOUTH);

		_stateHierarchyBreadCrumbs.onHover((crumb, inputEvent) ->
		{
		});

		_stateHierarchyBreadCrumbs.onSelect((crumb, inputEvent) ->
		{
			StateCrumb sc = (StateCrumb) crumb;
			if (sc != null)
				_pane.setModel(ModelPrimitive.getChildModel(sc.state));
		});

		updateLaf();

		PersistenceService persistenceService = theProject.getService(PersistenceService.class);
		if (persistenceService != null)
		{
			setConfiguration(persistenceService.getState());
		}
		_pane.addInteractionListener(new InteractionAdapter()
		{
			@Override
			public void hierarchyChanged()
			{
				updatedStateBreadcrumbs();
			}

		});
		GraphLafManagerListener.addGraphLafListener(_lafListener);

		_pane.setDialogHandler(new DialogHandler());
	}

	/**
	 * The Laf listener.
	 */
	protected LafListener _lafListener = new LafListener();

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

		_pane.setOpaque(true);
		_pane.getGraphConfiguration()._graphBackground = background;
		_pane.setBackground(background);

		_stateHierarchyBreadCrumbs.setBackground(background);

		Font font = getFont();
		if (font == null)
			font = new Font(Font.DIALOG, Font.PLAIN, 12);

		FontMetrics fontMetrics = getFontMetrics(font);

		_startStyle._linePaint = textAttribute.getForegroundColor();
		_startStyle._fillPaint = textAttribute.getForegroundColor();

		_stateOutlineStyle._linePaint = textAttribute.getForegroundColor();
		_stateOutlineStyle._fillPaint = textAttribute.getBackgroundColor();
		_stateOutlineStyle._lineStroke = new BasicStroke(2);
		_stateOutlineStyle._textPaint = textAttribute.getForegroundColor();

		_stateOutlineStyle._font = font;
		_stateOutlineStyle._fontMetrics = fontMetrics;

		_stateInnerStyle._linePaint = _stateOutlineStyle.getLinePaint();
		_stateInnerStyle._fillPaint = _stateOutlineStyle.getFillPaint();
		_stateInnerStyle._lineStroke = new BasicStroke(1);
		_stateInnerStyle._textPaint = _stateOutlineStyle.getTextPaint();
		_stateInnerStyle._background = background;
		_stateInnerStyle._font = font;
		_stateInnerStyle._fontMetrics = fontMetrics;

		_pane.getModel()
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
		_stateHierarchyCrumbs.clear();
		if (_root != null)
		{
			_stateHierarchyCrumbs.add(new StateCrumb(_root));
		}
		for (Visual v : _pane.getHierarchy())
		{
			_stateHierarchyCrumbs.add(new StateCrumb(v));
		}
		_stateHierarchyBreadCrumbs.setCrumbs(_stateHierarchyCrumbs);
	}

	/**
	 * Sets the FSM to show.
	 *
	 * @param fsm            The FSM to show.
	 * @param graphExtension The graph-extension or null
	 */
	public void setStateMachine(FiniteStateMachine fsm, ScxmlGraphExtension graphExtension)
	{
		super.setStateMachine(fsm, graphExtension);
		updatedStateBreadcrumbs();
	}

	/**
	 * In case the FSM could not be loaded, show the cause.
	 *
	 * @param cause The error cause, can be null.
	 */
	public void setError(Throwable cause)
	{
		_info.setText(cause == null ? "---" : cause.getLocalizedMessage());
	}
}

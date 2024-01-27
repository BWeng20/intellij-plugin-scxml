package com.bw.modelthings.intellij.editor;

import com.bw.modelthings.intellij.Icons;
import com.bw.modelthings.intellij.settings.ChangeConfigurationNotifier;
import com.bw.modelthings.intellij.settings.Configuration;
import com.bw.modelthings.intellij.settings.EditorLayout;
import com.bw.modelthings.intellij.settings.PersistenceService;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.JBSplitter;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.JBTabsFactory;
import com.intellij.ui.tabs.TabInfo;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;

/**
 * SCXML Editor with two sub-editors for manual XML editor and graphical SCXML editor.
 */
public class ScxmlEditor extends UserDataHolderBase implements FileEditor
{
	/**
	 * Message bus for change notification.
	 */
	private MessageBusConnection _mbCon;

	/**
	 * The editor component containing the two editors.
	 */
	JComponent _component;

	/**
	 * Graphical editor.
	 */
	ScxmlGraphEditor _scxmlEditor;

	/**
	 * Textual editor.
	 */
	TextEditor _xmlTextEditor;

	/**
	 * The file the editor is showing.
	 */
	final VirtualFile _file;

	/**
	 * The project the current file comes from.
	 */
	final Project _theProject;

	/**
	 * The layout of XML and Graph editor.
	 */
	EditorLayout _editorLayout = EditorLayout.Tabs;

	@Override
	@NotNull
	public VirtualFile getFile()
	{
		return _file;
	}

	private static final String PROPORTION_KEY = "ScxmlFileEditor.SplitProportion";

	/**
	 * Creates a new editor.
	 *
	 * @param file       The file to show.
	 * @param theProject The project.
	 */
	public ScxmlEditor(@NotNull VirtualFile file, @NotNull Project theProject)
	{
		this._file = file;
		this._theProject = theProject;

		PersistenceService persistenceService = theProject.getService(PersistenceService.class);
		if (persistenceService != null)
		{
			_editorLayout = persistenceService.getState()._editorLayout;
		}

		_mbCon = theProject.getMessageBus()
						   .connect();
		_mbCon.subscribe(ChangeConfigurationNotifier.CHANGE_CONFIG_TOPIC, (ChangeConfigurationNotifier) this::setConfiguration);

		createComponent(theProject);

	}


	@Override
	public @NotNull JComponent getComponent()
	{
		return _component;
	}

	@Override
	public @Nullable JComponent getPreferredFocusedComponent()
	{
		return _scxmlEditor.getComponent();
	}

	@Override
	public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName()
	{
		return "SCXML";
	}

	@Override
	public void setState(@NotNull FileEditorState state)
	{
		if (state instanceof ScxmlEditorState)
		{
			final ScxmlEditorState compositeState = (ScxmlEditorState) state;
			if (compositeState._xmlEditorState != null)
			{
				// xmlTextEditor.setState(compositeState.xmlEditorState);
			}
			if (compositeState._scxmlEditorState != null)
			{
				// scxmlEditor.setState(compositeState.scxmlEditorState;
			}
		}

	}

	@Override
	public boolean isModified()
	{
		return _xmlTextEditor.isModified() || _scxmlEditor.isModified();
	}

	@Override
	public boolean isValid()
	{
		return _xmlTextEditor.isValid() && _scxmlEditor.isValid();
	}

	@Override
	public void addPropertyChangeListener(@NotNull PropertyChangeListener listener)
	{
		_xmlTextEditor.addPropertyChangeListener(listener);
		_scxmlEditor.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(@NotNull PropertyChangeListener listener)
	{
		_xmlTextEditor.removePropertyChangeListener(listener);
		_scxmlEditor.removePropertyChangeListener(listener);
	}

	@Override
	public void dispose()
	{
		Disposer.dispose(_xmlTextEditor);
		Disposer.dispose(_scxmlEditor);
		Disposer.dispose(_mbCon);
	}

	/**
	 * Creates the editor component.
	 *
	 * @param project The current project.
	 */
	protected void createComponent(@NotNull Project project)
	{
		_scxmlEditor = new ScxmlGraphEditor(_file, PsiManager.getInstance(project)
															 .findFile(_file));

		_xmlTextEditor = (TextEditor) TextEditorProvider.getInstance()
														.createEditor(project, _file);

		_component = new JPanel(new BorderLayout());

		applyLayout();
	}

	/**
	 * layout the editors.
	 */
	protected void applyLayout()
	{
		_component.removeAll();

		switch (_editorLayout)
		{
			default:
			case Tabs:
			{
				JBTabs tabs = JBTabsFactory.createEditorTabs(_theProject, this);
				TabInfo xmlTabInfo = new TabInfo(_xmlTextEditor.getComponent());
				xmlTabInfo.setText("XML");
				tabs.addTab(xmlTabInfo);

				TabInfo graphTabInfo = new TabInfo(_scxmlEditor.getComponent());
				graphTabInfo.setIcon(Icons.STATE_MACHINE);

				tabs.addTab(graphTabInfo);
				tabs.select(graphTabInfo, false);
				_component.add(tabs.getComponent(), BorderLayout.CENTER);
			}
			break;
			case SplitHorizontal:
			{
				JBSplitter splitter = new JBSplitter(false);
				splitter.setFirstComponent(_xmlTextEditor.getComponent());
				splitter.setSecondComponent(_scxmlEditor.getComponent());
				_component.add(splitter, BorderLayout.CENTER);
			}
			break;
			case SplitVertical:
			{
				JBSplitter splitter = new JBSplitter(true);
				splitter.setFirstComponent(_xmlTextEditor.getComponent());
				splitter.setSecondComponent(_scxmlEditor.getComponent());
				_component.add(splitter, BorderLayout.CENTER);
			}
			break;
		}


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
			if (_scxmlEditor != null && _scxmlEditor._component != null)
			{
				_scxmlEditor._component.setConfiguration(config);
			}

			if (config._editorLayout != _editorLayout)
			{
				_editorLayout = config._editorLayout;
				applyLayout();
			}
		}
	}
}

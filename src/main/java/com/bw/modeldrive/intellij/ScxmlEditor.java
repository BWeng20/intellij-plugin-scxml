package com.bw.modeldrive.intellij;

import com.bw.modeldrive.intellij.editor.ScxmlGraphEditor;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.TextEditor;
import com.intellij.openapi.fileEditor.impl.text.TextEditorProvider;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiManager;
import com.intellij.ui.tabs.JBTabs;
import com.intellij.ui.tabs.JBTabsFactory;
import com.intellij.ui.tabs.TabInfo;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import java.beans.PropertyChangeListener;

/**
 * SCXML Editor with two sub-editors for manual XML editor and graphical SCXML editor.
 */
public class ScxmlEditor extends UserDataHolderBase implements FileEditor
{
	/**
	 * The editor component containing the two editors.
	 */
	JComponent component;

	/**
	 * The splitter between the editors.
	 */
	JBTabs tabs;

	/**
	 * Graphical editor.
	 */
	FileEditor scxmlEditor;

	/**
	 * Textual editor.
	 */
	TextEditor xmlTextEditor;

	/**
	 * The file the editor is showing.
	 */
	final VirtualFile file;

	@Override
	@NotNull
	public VirtualFile getFile()
	{
		return file;
	}

	private static final String PROPORTION_KEY = "ScxmlFileEditor.SplitProportion";

	/**
	 * Creates a new editor.
	 *
	 * @param file    The file to show.
	 * @param project The project.
	 */
	public ScxmlEditor(@NotNull VirtualFile file, @NotNull Project project)
	{
		this.file = file;
		component = createComponent(project);
	}


	@Override
	public @NotNull JComponent getComponent()
	{
		return component;
	}

	@Override
	public @Nullable JComponent getPreferredFocusedComponent()
	{
		return scxmlEditor.getComponent();
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
			if (compositeState.xmlEditorState != null)
			{
				// xmlTextEditor.setState(compositeState.xmlEditorState);
			}
			if (compositeState.scxmlEditorState != null)
			{
				// scxmlEditor.setState(compositeState.scxmlEditorState;
			}
		}

	}

	@Override
	public boolean isModified()
	{
		return xmlTextEditor.isModified() || scxmlEditor.isModified();
	}

	@Override
	public boolean isValid()
	{
		return xmlTextEditor.isValid() && scxmlEditor.isValid();
	}

	@Override
	public void addPropertyChangeListener(@NotNull PropertyChangeListener listener)
	{
		xmlTextEditor.addPropertyChangeListener(listener);
		scxmlEditor.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(@NotNull PropertyChangeListener listener)
	{
		xmlTextEditor.removePropertyChangeListener(listener);
		scxmlEditor.removePropertyChangeListener(listener);
	}

	@Override
	public void dispose()
	{
		Disposer.dispose(xmlTextEditor);
		Disposer.dispose(scxmlEditor);
	}

	/**
	 * Creates the editor component.
	 *
	 * @param project The current project.
	 * @return The UI component for the editor.
	 */
	protected JComponent createComponent(@NotNull Project project)
	{
		tabs = JBTabsFactory.createEditorTabs(project, this);

		scxmlEditor = new ScxmlGraphEditor(file, PsiManager.getInstance(project)
														   .findFile(file));

		xmlTextEditor = (TextEditor) TextEditorProvider.getInstance()
													   .createEditor(project, file);

		TabInfo xmlTabInfo = new TabInfo(xmlTextEditor.getComponent());
		xmlTabInfo.setText("XML");
		tabs.addTab(xmlTabInfo);

		TabInfo graphTabInfo = new TabInfo(scxmlEditor.getComponent());
		graphTabInfo.setIcon(Icons.STATE_MACHINE);

		tabs.addTab(graphTabInfo);
		tabs.select(graphTabInfo, false);
		return tabs.getComponent();

	}
}

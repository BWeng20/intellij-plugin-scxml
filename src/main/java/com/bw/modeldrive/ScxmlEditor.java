package com.bw.modeldrive;

import com.bw.modeldrive.editor.ScxmlGraphEditor;
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
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.beans.PropertyChangeListener;

public class ScxmlEditor extends UserDataHolderBase implements FileEditor
{
	JComponent component;
	JBSplitter splitter;
	FileEditor scxmlEditor;
	TextEditor xmlTextEditor;
	final VirtualFile file;

	@Override
	@NotNull
	public VirtualFile getFile()
	{
		return file;
	}

	private static final String PROPORTION_KEY = "ScxmlFileEditor.SplitProportion";

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
			if (compositeState.splitterLayout != null)
			{
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
		splitter = new JBSplitter(false, 0.5f, 0.15f, 0.85f);
		splitter.setSplitterProportionKey(PROPORTION_KEY);

		scxmlEditor = new ScxmlGraphEditor(file, PsiManager.getInstance(project)
														   .findFile(file));
		xmlTextEditor = (TextEditor) TextEditorProvider.getInstance()
													   .createEditor(project, file);

		splitter.setFirstComponent(xmlTextEditor.getComponent());
		splitter.setSecondComponent(scxmlEditor.getComponent());

		JPanel toolbar = new JPanel();

		final JPanel result = new JPanel(new BorderLayout());
		result.add(toolbar, BorderLayout.NORTH);
		result.add(splitter, BorderLayout.CENTER);

		return result;

	}
}

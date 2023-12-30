package com.bw.modeldrive;

import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorPolicy;
import com.intellij.openapi.fileEditor.FileEditorProvider;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

/**
 * Editor provider that accepts files with extension "scxml".
 */
public class ScxmlEditorProvider implements FileEditorProvider, DumbAware
{
	/**
	 * Creates a new editor provider.
	 */
	public ScxmlEditorProvider()
	{

	}

	@Override
	public boolean accept(@NotNull Project project, @NotNull VirtualFile file)
	{
		return "scxml".equalsIgnoreCase(file.getExtension());
	}

	@Override
	public @NotNull FileEditor createEditor(@NotNull Project project, @NotNull VirtualFile file)
	{
		ScxmlEditor scxmlEditor = new ScxmlEditor(file, project);
		return scxmlEditor;
	}

	@Override
	public @NotNull @NonNls String getEditorTypeId()
	{
		return "scxml-editor";
	}

	@Override
	public @NotNull FileEditorPolicy getPolicy()
	{
		return FileEditorPolicy.HIDE_DEFAULT_EDITOR;
	}
}

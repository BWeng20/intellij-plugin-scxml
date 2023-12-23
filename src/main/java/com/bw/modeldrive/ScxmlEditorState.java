package com.bw.modeldrive;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import org.jetbrains.annotations.Nullable;

/**
 * Editor state that handles scxml sub-editors.
 */
public class ScxmlEditorState implements FileEditorState
{
	public final String splitterLayout;
	public final FileEditorState xmlEditorState;
	public final FileEditorState scxmlEditorState;

	public ScxmlEditorState(@Nullable String splitterLayout, @Nullable FileEditorState xmlEditorState, @Nullable FileEditorState scxmlEditorState)
	{
		this.splitterLayout = splitterLayout;
		this.xmlEditorState = xmlEditorState;
		this.scxmlEditorState = scxmlEditorState;
	}

	@Override
	public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level)
	{
		return otherState instanceof ScxmlEditorState
				&& (xmlEditorState == null || xmlEditorState.canBeMergedWith(((ScxmlEditorState) otherState).xmlEditorState, level))
				&& (scxmlEditorState == null || scxmlEditorState.canBeMergedWith(((ScxmlEditorState) otherState).scxmlEditorState, level));
	}
}

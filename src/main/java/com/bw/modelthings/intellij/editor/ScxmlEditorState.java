package com.bw.modelthings.intellij.editor;

import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.fileEditor.FileEditorStateLevel;
import org.jetbrains.annotations.Nullable;

/**
 * Editor state that handles scxml sub-editors.
 */
public class ScxmlEditorState implements FileEditorState
{
	/**
	 * State of textual editor.
	 */
	public final FileEditorState _xmlEditorState;

	/**
	 * State of graphical editor.
	 */
	public final FileEditorState _scxmlEditorState;

	/**
	 * Creates a new state from components
	 *
	 * @param xmlEditorState   The state of the textual editor.
	 * @param scxmlEditorState The state of the graphical editor.
	 */
	public ScxmlEditorState(@Nullable FileEditorState xmlEditorState, @Nullable FileEditorState scxmlEditorState)
	{
		this._xmlEditorState = xmlEditorState;
		this._scxmlEditorState = scxmlEditorState;
	}

	@Override
	public boolean canBeMergedWith(FileEditorState otherState, FileEditorStateLevel level)
	{
		return otherState instanceof ScxmlEditorState
				&& (_xmlEditorState == null || _xmlEditorState.canBeMergedWith(((ScxmlEditorState) otherState)._xmlEditorState, level))
				&& (_scxmlEditorState == null || _scxmlEditorState.canBeMergedWith(((ScxmlEditorState) otherState)._scxmlEditorState, level));
	}
}

package com.bw.graph.editor;

import com.bw.graph.primitive.DrawPrimitive;

import java.awt.Component;

/**
 * Editor proxy to edit DrawPrimitives and associated data models.<br>
 * Supports edits if used as user-data in DrawPrimitives.
 */
public interface EditorProxy
{
	/**
	 * Gets the editor component.
	 * @param primitive The primitive to edit.
	 * @return The editor component, with preset data.
	 */
	Component getEditor(DrawPrimitive primitive);

	/**
	 * Ends and commits edit.
	 * @param primitive The primitive to update.
	 */
	void endEdit(DrawPrimitive primitive);

	void cancelEdit(DrawPrimitive primitive);
}

package com.bw.graph.editor;

import com.bw.graph.primitive.DrawPrimitive;

import javax.swing.JComponent;
import java.awt.Graphics2D;

/**
 * Editor proxy to edit DrawPrimitives and associated data models.<br>
 * Supports edits if used as user-data in DrawPrimitives.
 */
public interface EditorProxy
{
	/**
	 * Gets the editor component.
	 *
	 * @param primitive The primitive to edit.
	 * @return The editor component, with preset data.
	 */
	JComponent getEditor(DrawPrimitive primitive);

	/**
	 * Ends and commits edit.
	 * Also updates the containing visual if needed, e.g. adapts layouts.
	 *
	 * @param primitive The primitive to update.
	 * @param g2        Graphics context for calculations.
	 */
	void endEdit(DrawPrimitive primitive, Graphics2D g2);

	/**
	 * Aborts edit.
	 *
	 * @param primitive The primitive to restore to original.
	 */
	void cancelEdit(DrawPrimitive primitive);
}

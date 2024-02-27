package com.bw.graph.editor;

import com.bw.graph.VisualModel;
import com.bw.graph.editor.action.EditAction;
import com.bw.graph.primitive.DrawPrimitive;

import javax.swing.JComponent;
import java.awt.Graphics2D;

/**
 * Editor to in-place edit of DrawPrimitives and associated data models.<br>
 * Supports edits if used as user-data in DrawPrimitives.
 */
public interface Editor
{

	/**
	 * Can the editor be used in-place.
	 *
	 * @return true if the component shall be shown in-place.
	 */
	boolean isInPlace();

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
	 * @param model     The model.
	 * @param g2        Graphics context for calculations.
	 * @return The edit action of the edit.
	 */
	EditAction endEdit(DrawPrimitive primitive, VisualModel model, Graphics2D g2);

	/**
	 * Aborts edit.
	 *
	 * @param primitive The primitive to restore to original.
	 */
	void cancelEdit(DrawPrimitive primitive);
}

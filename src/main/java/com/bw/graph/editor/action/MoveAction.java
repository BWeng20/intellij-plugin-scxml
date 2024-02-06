package com.bw.graph.editor.action;

import com.bw.graph.visual.Visual;

import java.awt.geom.Point2D;

/**
 * State move action.
 */
public class MoveAction implements EditAction
{
	/**
	 * The original position.
	 */
	public Point2D.Float _from;

	/**
	 * The destination position.
	 */
	public Point2D.Float _to;

	/**
	 * The visual that was moved.
	 */
	public Visual _what;

	public boolean _relative;

	/**
	 * Creates a new move action.
	 *
	 * @param visual The visual that was moved.
	 * @param from   The original position.
	 * @param to     The destination.
	 */
	public MoveAction(Visual visual, Point2D.Float from, Point2D.Float to)
	{
		this(visual, from, to, false);
	}

	/**
	 * Creates a new move action.
	 *
	 * @param visual   The visual that was moved.
	 * @param from     The original position.
	 * @param to       The destination.
	 * @param relative If true the move is relative, otherwise absolute.
	 */
	public MoveAction(Visual visual, Point2D.Float from, Point2D.Float to, boolean relative)
	{
		_from = new Point2D.Float(from.x, from.y);
		_to = new Point2D.Float(to.x, to.y);
		_what = visual;
		relative = _relative;
	}
}

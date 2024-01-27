package com.bw.graph.util;

/**
 * The class represents Insets as float values.
 */
public final class InsetsFloat
{

	/**
	 * The inset from the top.
	 */
	public float _top;

	/**
	 * The inset from the left.
	 */
	public float _left;

	/**
	 * The inset from the bottom.
	 */
	public float _bottom;

	/**
	 * The inset from the right.
	 */
	public float _right;

	/**
	 * Creates and initializes a new {@code InsetsFloat} object.
	 *
	 * @param top    the inset from the top.
	 * @param left   the inset from the left.
	 * @param bottom the inset from the bottom.
	 * @param right  the inset from the right.
	 */
	public InsetsFloat(float top, float left, float bottom, float right)
	{
		this._top = top;
		this._left = left;
		this._bottom = bottom;
		this._right = right;
	}

	@Override
	public String toString()
	{
		return "Inserts[" + _top + "," + _left + "," + _bottom + "," + _right + "]";
	}
}

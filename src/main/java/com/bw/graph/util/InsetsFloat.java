package com.bw.graph.util;

/**
 * The class represents Insets as float values.
 */
public final class InsetsFloat
{

	/**
	 * The inset from the top.
	 */
	public float top;

	/**
	 * The inset from the left.
	 */
	public float left;

	/**
	 * The inset from the bottom.
	 */
	public float bottom;

	/**
	 * The inset from the right.
	 */
	public float right;

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
		this.top = top;
		this.left = left;
		this.bottom = bottom;
		this.right = right;
	}
}

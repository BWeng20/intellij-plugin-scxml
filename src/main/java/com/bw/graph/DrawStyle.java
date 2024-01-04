package com.bw.graph;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Paint;
import java.awt.Stroke;

/**
 * Visual style information to use for drawing operations.
 *
 * @see DrawContext
 */
public class DrawStyle
{
	/**
	 * Create a new empty DrawStyle.
	 */
	public DrawStyle()
	{

	}

	public Orientation orientation;

	/**
	 * True if highlighted.
	 */
	public boolean highlighted;

	/**
	 * Paint for lines
	 */
	public Paint linePaint;

	/**
	 * Paint for fill
	 */
	public Paint fillPaint;


	/**
	 * Paint for text
	 */

	public Paint textPaint;

	/**
	 * Stroke for lines
	 */
	public Stroke lineStroke;

	/**
	 * Font
	 */
	public Font font;

	/**
	 * Font Metrics
	 */
	public FontMetrics fontMetrics;
}

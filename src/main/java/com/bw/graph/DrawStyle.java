package com.bw.graph;

import java.awt.BasicStroke;
import java.awt.Color;
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
	 * Default stroke.
	 */
	public final static BasicStroke defaultStroke = new BasicStroke(1);

	/**
	 * Create a new empty DrawStyle.
	 */
	public DrawStyle()
	{

	}

	/**
	 * The alignment. Can be null.
	 */
	public Alignment alignment;

	/**
	 * True if highlighted.
	 */
	public boolean highlighted;

	/**
	 * Paint for lines
	 */
	public Paint linePaint = Color.BLACK;

	/**
	 * Paint for fill
	 */
	public Paint fillPaint = Color.GRAY;


	/**
	 * Paint for text
	 */

	public Paint textPaint = Color.BLACK;

	/**
	 * Stroke for lines
	 */
	public Stroke lineStroke = defaultStroke;

	/**
	 * Font
	 */
	public Font font;

	/**
	 * Font Metrics
	 */
	public FontMetrics fontMetrics;

	/**
	 * Get stroke width.
	 *
	 * @return The width in graphic units.
	 */
	public float getStrokeWidth()
	{
		if (lineStroke instanceof BasicStroke)
			return ((BasicStroke) lineStroke).getLineWidth();
		else
			return 1f;
	}
}

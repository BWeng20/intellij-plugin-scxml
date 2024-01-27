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
	public final static BasicStroke DEFAULT_STROKE = new BasicStroke(1);

	/**
	 * Create a new empty DrawStyle.
	 */
	public DrawStyle()
	{

	}

	/**
	 * Paint for lines
	 */
	public Paint _linePaint = Color.BLACK;

	/**
	 * Paint for fill
	 */
	public Paint _fillPaint = Color.GRAY;


	/**
	 * Paint for background
	 */
	public Paint _background = Color.LIGHT_GRAY;


	/**
	 * Paint for text
	 */

	public Paint _textPaint = Color.BLACK;

	/**
	 * Stroke for lines
	 */
	public Stroke _lineStroke = DEFAULT_STROKE;

	/**
	 * Font
	 */
	public Font _font;

	/**
	 * Font Metrics
	 */
	public FontMetrics _fontMetrics;

	/**
	 * Get stroke width.
	 *
	 * @return The width in graphic units.
	 */
	public float getStrokeWidth()
	{
		if (_lineStroke instanceof BasicStroke)
			return ((BasicStroke) _lineStroke).getLineWidth();
		else
			return 1f;
	}
}

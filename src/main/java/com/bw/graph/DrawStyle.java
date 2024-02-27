package com.bw.graph;

import java.awt.BasicStroke;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Paint;
import java.awt.Stroke;

/**
 * Visual style information to use for drawing operations.<br>
 * Primitives shall share instances if possible.<br>
 *
 * @see DrawContext
 */
public interface DrawStyle
{
	/**
	 * Default stroke.
	 */
	BasicStroke DEFAULT_STROKE = new BasicStroke(1);

	/**
	 * Get stroke width.
	 *
	 * @return The width in graphic units.
	 */
	float getStrokeWidth();

	/**
	 * Paint for lines
	 *
	 * @return The Paint.
	 */
	Paint getLinePaint();

	/**
	 * Paint for fill
	 *
	 * @return The paint.
	 */
	Paint getFillPaint();

	/**
	 * Paint for background
	 *
	 * @return The Paint.
	 */
	Paint getBackground();

	/**
	 * Paint for text
	 *
	 * @return The Paint.
	 */
	Paint getTextPaint();

	/**
	 * Stroke for lines
	 *
	 * @return The Stroke.
	 */
	Stroke getLineStroke();

	/**
	 * The Font for text.
	 *
	 * @return The font or null.
	 */
	Font getFont();

	/**
	 * Font Metrics of the font.
	 *
	 * @return The font or null.
	 */
	FontMetrics getFontMetrics();
}

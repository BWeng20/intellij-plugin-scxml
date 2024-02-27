package com.bw.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Paint;
import java.awt.Stroke;

/**
 * Simple implementation for draw styles.<br>
 */
public final class SimpleDrawStyle implements DrawStyle
{

	/**
	 * Create a new empty DrawStyle.
	 */
	public SimpleDrawStyle()
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

	@Override
	public float getStrokeWidth()
	{
		if (_lineStroke instanceof BasicStroke)
			return ((BasicStroke) _lineStroke).getLineWidth();
		else
			return 1f;
	}

	@Override
	public Paint getLinePaint()
	{
		return _linePaint;
	}

	@Override
	public Paint getFillPaint()
	{
		return _fillPaint;
	}

	@Override
	public Paint getBackground()
	{
		return _background;
	}

	@Override
	public Paint getTextPaint()
	{
		return _textPaint;
	}

	@Override
	public Stroke getLineStroke()
	{
		return _lineStroke;
	}

	@Override
	public Font getFont()
	{
		return _font;
	}

	/**
	 * Font Metrics
	 */
	@Override
	public FontMetrics getFontMetrics()
	{
		return _fontMetrics;
	}
}

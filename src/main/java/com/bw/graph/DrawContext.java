package com.bw.graph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;

/**
 * Drawing context for Visuals, containing style information.
 */
public class DrawContext
{

	/**
	 * Style for normal state.
	 */
	public DrawStyle normal;

	/**
	 * Style for highlighted state.
	 */
	public DrawStyle highlighted;

	/**
	 * Create a new context with default values.
	 */
	public DrawContext()
	{
		normal = new DrawStyle();
		normal.font = Font.getFont(Font.DIALOG);
		normal.textPaint = Color.BLACK;
		normal.linePaint = Color.BLACK;
		normal.lineStroke = new BasicStroke(1);

		highlighted = new DrawStyle();
		highlighted.font = Font.getFont(Font.DIALOG);
		highlighted.textPaint = Color.BLACK;
		highlighted.linePaint = Color.BLACK;
		highlighted.lineStroke = new BasicStroke(2);
	}

	/**
	 * Creates a new Context with styles,
	 *
	 * @param normalStyle    Style for normal state.
	 * @param highlightStyle Style for highlighted state.
	 */
	public DrawContext(DrawStyle normalStyle, DrawStyle highlightStyle)
	{
		this.normal = normalStyle;
		this.highlighted = highlightStyle;
	}
}

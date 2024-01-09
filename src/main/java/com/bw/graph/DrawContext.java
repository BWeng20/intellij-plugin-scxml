package com.bw.graph;

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
	 * The configuration.
	 */
	public final GraphConfiguration configuration;

	/**
	 * Creates a new Context with styles,
	 *
	 * @param configuration  Graph configuration to use.
	 * @param normalStyle    Style for normal state.
	 * @param highlightStyle Style for highlighted state.
	 */
	public DrawContext(GraphConfiguration configuration, DrawStyle normalStyle, DrawStyle highlightStyle)
	{
		this.normal = normalStyle;
		this.highlighted = highlightStyle;
		this.configuration = configuration;
	}
}

package com.bw.graph;

/**
 * Drawing context for Visuals, containing style information.
 */
public class DrawContext
{
	/**
	 * Style for drawings.
	 */
	public DrawStyle style;

	/**
	 * The configuration.
	 */
	public final GraphConfiguration configuration;

	/**
	 * Creates a new Context with styles,
	 *
	 * @param configuration Graph configuration to use.
	 * @param style         Style.
	 */
	public DrawContext(GraphConfiguration configuration, DrawStyle style)
	{
		this.style = style;
		this.configuration = configuration;
	}
}

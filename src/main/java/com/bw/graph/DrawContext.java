package com.bw.graph;

/**
 * Drawing context for Visuals, containing style information.
 */
public class DrawContext
{
	/**
	 * Style for drawings.
	 */
	public DrawStyle _style;

	/**
	 * The configuration.
	 */
	public final GraphConfiguration _configuration;

	/**
	 * Creates a new Context with styles,
	 *
	 * @param configuration Graph configuration to use.
	 * @param style         Style.
	 */
	public DrawContext(GraphConfiguration configuration, DrawStyle style)
	{
		this._style = style;
		this._configuration = configuration;
	}
}

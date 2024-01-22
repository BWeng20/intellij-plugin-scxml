package com.bw.svg;

import java.util.HashMap;

/**
 * SVG Tag names.
 */
@SuppressWarnings("doclint:missing")
public enum SVGElement
{
	/**
	 * Group element
	 */
	g,

	/**
	 * Anchor
	 */
	a,

	/**
	 * Clip path
	 */
	clipPath,

	/**
	 * Path
	 */
	path,

	/**
	 * Rectangle
	 */
	rect,

	/**
	 * Circle
	 */
	circle,

	/**
	 * Ellipse
	 */
	ellipse,

	/**
	 * Text
	 */
	text,

	/**
	 * Text path
	 */
	textPath,

	/**
	 * line
	 */
	line,

	/**
	 * Polyline
	 */
	polyline,

	/**
	 * Polygon
	 */
	polygon,

	/**
	 * Use reference
	 */
	use,

	/**
	 * Style sheet definition
	 */
	style,

	/**
	 * Definitions
	 */
	defs,

	/**
	 * Gradient definitions.
	 */
	linearGradient, radialGradient,

	/**
	 * Filter definition
	 */
	filter,

	/**
	 * Marker definition
	 */
	marker,

	/**
	 * Document title
	 */
	title,

	/**
	 * Description
	 */
	desc,

	/**
	 * Sub element for feComponentTransfer
	 */
	feFuncA, feFuncB, feFuncG, feFuncR,

	/**
	 * Sub element for feMerge
	 */
	feMergeNode,

	/**
	 * Filter primitive elements
	 */
	feBlend, feColorMatrix, feComponentTransfer, feComposite,
	feConvolveMatrix, feDiffuseLighting, feDisplacementMap, feDropShadow,
	feFlood, feGaussianBlur, feImage, feMerge, feMorphology, feOffset,
	feSpecularLighting, feNop, feTile, feTurbulence,

	/**
	 * Sub element of feDiffuseLighting and feSpecularLighting.
	 */
	feDistantLight, fePointLight, feSpotLight,

	/**
	 * meta data definition.
	 */
	metadata;

	private final static HashMap<String, SVGElement> elements_ = new HashMap<>();

	// Use map instead of "valueOf" to avoid exceptions for unknown values
	static
	{
		for (SVGElement t : values())
			elements_.put(t.name(), t);
	}

	/**
	 * Gets the enum value by name.
	 *
	 * @param elementName The xml name.
	 * @return The element or null.
	 */
	public static SVGElement valueFrom(String elementName)
	{
		return elements_.get(elementName);
	}

}
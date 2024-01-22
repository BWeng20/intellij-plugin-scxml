package com.bw.svg;

import java.util.HashMap;

/**
 * SVG Attributes
 */
@SuppressWarnings("doclint:missing")
public enum SVGAttribute
{
	Class("class"),
	Color("color"),
	BackgroundColor("background-color"),
	ViewBox("viewBox"),
	ClipPath("clip-path"),
	D("d"),
	Opacity("opacity"),
	Marker_Start("marker-start"),
	Marker_Mid("marker-mid"),
	Marker_End("marker-end"),
	Font_Weight("font-weight"),

	Rx("rx"),
	Ry("ry"),
	Stroke("stroke"),
	StrokeOpacity("stroke-opacity"),
	StrokeWidth("stroke-width"),
	StrokeDashArray("stroke-dasharray"),
	StrokeDashOffset("stroke-dashoffset"),
	StrokeLineCap("stroke-linecap"),
	StrokeLineJoin("stroke-linejoin"),
	StrokeMiterLimit("stroke-miterlimit"),
	Style("style"),
	Transform("transform"),
	Width("width"),
	Height("height"),
	FilterUnits("filterUnits"),
	PrimitiveUnits("primitiveUnits"),

	ColorInterpolationFilters("color-interpolation-filters"),

	In("in"),
	In2("in2"),
	Result("result"),
	Dx("dx"), Dy("dy"),
	X("x"), Y("y"),
	SpreadMethod("spreadMethod"),

	FillOpacity("fill-opacity"),
	StdDeviation("stdDeviation"),

	GradientUnits("gradientUnits"),
	GradientTransform("gradientTransform"),
	Offset("offset"),
	StopColor("stop-color"),
	StopOpacity("stop-opacity"),
	Fill("fill"),
	FillRule("fill-rule"),

	X1("x1"),
	Y1("y1"),
	X2("x2"),
	Y2("y2"),

	Points("points"),

	Cx("cx"),
	Cy("cy"),
	R("r"),

	WhiteSpace("white-space"),
	XmlSpace("xml:space"),
	FontSize("font-size"),
	FontFamily("font-family"),

	RefX("refX"),
	RefY("refY"),

	MarkerWidth("markerWidth"),
	MarkerHeight("markerHeight"),
	MarkerUnits("markerUnits"),
	Orient("orient"),

	StartOffset("startOffset"),
	TextLength("textLength"),
	LengthAdjust("lengthAdjust"),
	TextAnchor("text-anchor"),

	Fx("fx"),
	Fy("fy"),
	Fr("fr");

	private final static HashMap<String, SVGAttribute> attributes_ = new HashMap<>();

	// Use map instead of "valueOf" to avoid exceptions for unknown values
	static
	{
		for (SVGAttribute t : values())
			attributes_.put(t.xmlName(), t);
	}

	private final String xmlName_;

	SVGAttribute(String xmlName)
	{
		xmlName_ = xmlName;
	}

	public static SVGAttribute valueFrom(String attributeName)
	{
		return attributes_.get(attributeName);
	}

	public String xmlName()
	{
		return xmlName_;
	}

}

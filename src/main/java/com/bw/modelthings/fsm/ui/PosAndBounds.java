package com.bw.modelthings.fsm.ui;

import com.bw.svg.SVGWriter;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.logging.Level;
import java.util.regex.Pattern;

/**
 * Holds a position and the bounding-rectangle.
 */
public class PosAndBounds
{
	/**
	 * The bounds.
	 */
	public final Rectangle2D.Float bounds;

	/**
	 * The position.
	 */
	public final Point2D.Float position;

	/**
	 * Creates a new instance.
	 *
	 * @param pos    The position.
	 * @param bounds The bounds.
	 */
	public PosAndBounds(Point2D.Float pos, Rectangle2D.Float bounds)
	{
		this.bounds = bounds;
		this.position = pos;
	}

	/**
	 * Gets the XML representation.
	 * A sequence of six float values, separated by white-space.
	 *
	 * @param precisionFactor The precision factor to use.
	 * @return The string.
	 */
	public String toXML(float precisionFactor)
	{
		return SVGWriter.toPoint(position, precisionFactor) + " " +
				SVGWriter.toBox(bounds, precisionFactor);
	}

	@Override
	public String toString()
	{
		return toXML(1000);
	}

	private final static Pattern SPLIT_REG_EXP = Pattern.compile("(?U)\\s");


	/**
	 * Parse a XML position and bound string.
	 *
	 * @param bounds The bound string.
	 * @return The bounds or null if the string was not correct.
	 */
	public static PosAndBounds parse(String bounds)
	{
		if (bounds != null)
		{
			String[] coordinate = SPLIT_REG_EXP.split(bounds, 0);
			if (coordinate.length == 6)
			{
				try
				{
					return new PosAndBounds(
							new Point2D.Float(
									Float.parseFloat(coordinate[0]),
									Float.parseFloat(coordinate[1])
							),
							new Rectangle2D.Float(
									Float.parseFloat(coordinate[2]),
									Float.parseFloat(coordinate[3]),
									Float.parseFloat(coordinate[4]),
									Float.parseFloat(coordinate[5]))
					);
				}
				catch (NumberFormatException e)
				{
					ScxmlGraphExtension.log.log(Level.WARNING, "bounds could not be parsed", e);
				}
			}
		}
		return null;
	}

	/**
	 * Parse a XML position string.
	 *
	 * @param position The position string.
	 * @return The point or null if the string was not correct.
	 */
	public static Point2D.Float parsePosition(String position)
	{
		if (position != null)
		{
			String[] coordinate = SPLIT_REG_EXP.split(position, 0);
			if (coordinate.length == 2)
			{
				try
				{
					return new Point2D.Float(
							Float.parseFloat(coordinate[0]),
							Float.parseFloat(coordinate[1])
					);
				}
				catch (NumberFormatException e)
				{
					ScxmlGraphExtension.log.log(Level.WARNING, "position could not be parsed", e);
				}
			}
		}
		return null;
	}

}

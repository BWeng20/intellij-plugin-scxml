package com.bw.graph;

import java.awt.GraphicsConfiguration;
import java.awt.GraphicsEnvironment;
import java.awt.Transparency;
import java.awt.image.BufferedImage;

/**
 * Tool class for buffer image creation.
 */
public interface ImageUtil
{
	/**
	 * The graphic configuration to create images for.
	 */
	GraphicsConfiguration graphicsConfiguration = GraphicsEnvironment
			.getLocalGraphicsEnvironment()
			.getDefaultScreenDevice()
			.getDefaultConfiguration();

	/**
	 * Creates an image that is compatible with the default graphic configuration.
	 *
	 * @param width  The width in pixel.
	 * @param height The height in pixel.
	 * @return The new image.
	 */
	static BufferedImage createCompatibleImage(double width, double height)
	{
		return createCompatibleImage(graphicsConfiguration, width, height);
	}

	/**
	 * Creates an image that is compatible with the given graphic configuration.
	 *
	 * @param cfg    The graphics configuration.
	 * @param width  The width in pixel.
	 * @param height The height in pixel.
	 * @return The new image.
	 */
	static BufferedImage createCompatibleImage(GraphicsConfiguration cfg, double width, double height)
	{
		return cfg.createCompatibleImage((int) Math.ceil(width), (int) Math.ceil(height), Transparency.TRANSLUCENT);
	}
}

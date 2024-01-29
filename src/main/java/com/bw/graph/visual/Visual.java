package com.bw.graph.visual;

import com.bw.graph.DrawContext;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.editor.EditorProxy;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;

/**
 * A visual representation in the graph.
 */
public abstract class Visual
{
	/**
	 * Identification object from caller. Can be null.
	 */
	protected Object _id;

	/**
	 * Name for UI. If null, string-representation of {@link #_id} is used.<br>
	 * Can be used of the id is not suitable for UI.
	 *
	 * @see #getDisplayName()
	 */
	protected String _displayName;

	/**
	 * Combination of flag bits.
	 */
	protected int _flags;

	/**
	 * The preferred dimension of the visual.
	 */
	protected Dimension2DFloat _dimension;

	/**
	 * Absolute base position of the visual.
	 */
	protected Point2D.Float _absolutePosition = new Point2D.Float(0, 0);

	/**
	 * The absolute bounds of the visual.<br>
	 * These bounds are lazy calculated and updated. The bounds may extend the {@link #_absolutePosition} in all directions.
	 * The resulting bounds result from the absolute {@link #_absolutePosition} and
	 * the combined dimension of the content. But implementations may choose other options. See {@link #updateBounds(Graphics2D)}.
	 */
	protected Rectangle2D.Float _absoluteBounds = new Rectangle2D.Float(0, 0, -1, -1);

	/**
	 * The drawing context to use for painting and size calculations.
	 */
	protected DrawContext _context;

	/**
	 * True if offscreen buffers needs to be updated.
	 */
	protected boolean _offscreenBuffersInvalid = false;


	/**
	 * Create a new empty visual.
	 *
	 * @param id      The identification. Can be null.
	 * @param context The Drawing context to use.
	 */
	protected Visual(Object id, DrawContext context)
	{
		Objects.requireNonNull(context);
		this._id = id;
		this._context = context;
		this._flags = VisualFlags.ALWAYS;
	}


	/**
	 * Sets the diplay name.
	 *
	 * @param displayName The display-name or null to restore default behaviour.
	 * @see #getDisplayName()
	 */
	public void setDisplayName(String displayName)
	{
		this._displayName = displayName;
	}

	/**
	 * Draw the visual.
	 *
	 * @param g2 The Graphics context
	 */
	public void draw(Graphics2D g2)
	{
		g2.translate(_absolutePosition.x, _absolutePosition.y);
		try
		{
			drawRelative(g2);
			_offscreenBuffersInvalid = false;
		}
		finally
		{
			g2.translate(-_absolutePosition.x, -_absolutePosition.y);
		}
	}


	/**
	 * Draw the visual with graphics context moved to absolute base position.<br>
	 * If a sub-model is set, the area described by {@link GraphConfiguration#_innerModelBoxInsets} and {@link GraphConfiguration#_innerModelBoxMinDimension} shall be spared,
	 * as this area will be over-drawn by {@link #draw(Graphics2D)}.
	 *
	 * @param g2 The Graphics context
	 */
	protected abstract void drawRelative(Graphics2D g2);

	/**
	 * Checks if a point is inside the area of the visual.
	 *
	 * @param x X position.
	 * @param y Y position.
	 * @return true if (x,y) is inside the visual.
	 */
	public boolean containsPoint(float x, float y)
	{
		return _absoluteBounds.contains(x, y);
	}

	/**
	 * Updates {@link #_absoluteBounds}.
	 *
	 * @param graphics The graphics context to use for calculations.
	 */
	protected abstract void updateBounds(Graphics2D graphics);

	/**
	 * Moves the visual by some delta.
	 *
	 * @param x The X-Delta to move.
	 * @param y The Y-Delta to move.
	 */
	public void moveBy(float x, float y)
	{
		if (x != 0 || y != 0)
		{
			setModified();
			_absolutePosition.x += x;
			_absolutePosition.y += y;
			_absoluteBounds.x += x;
			_absoluteBounds.y += y;
		}
	}

	/**
	 * Triggers the re-calculation of bounds on next paint.
	 */
	public void resetBounds()
	{
		_absoluteBounds.width = -1;
	}

	/**
	 * Gets the bounds of the visual.
	 *
	 * @param g2 The Graphic context to use for calculations. Will not be modified.
	 * @return The bounds in absolute coordinates, never null.
	 */
	public Rectangle2D.Float getAbsoluteBounds2D(Graphics2D g2)
	{
		if (_absoluteBounds.width < 0 && g2 != null)
		{
			updateBounds(g2);
		}
		return new Rectangle2D.Float(_absoluteBounds.x, _absoluteBounds.y, _absoluteBounds.width, _absoluteBounds.height);
	}

	/**
	 * Sets Bit Flags.
	 *
	 * @param flags Flag bits to add.
	 */
	public void setFlags(int flags)
	{
		if ((flags | this._flags) != this._flags)
		{
			this._flags |= flags;
			invalidateBuffers();
		}
	}

	/**
	 * Checks if some (combination of) flag(s) are set.
	 *
	 * @param flags Bitwise combination of flags.
	 * @return true if all bits in the argument are set.
	 */
	public boolean isFlagSet(int flags)
	{
		return (this._flags & flags) == flags;
	}

	/**
	 * Clears some (combination of) flag(s).
	 *
	 * @param flags Flag bits to clear.
	 */
	public void clearFlags(int flags)
	{
		if ((this._flags & flags) != 0)
		{
			this._flags &= ~flags;
			invalidateBuffers();
		}
	}

	/**
	 * Gets the current flags.
	 *
	 * @return Bitwise combination of flag-bits.
	 */
	public int getFlags()
	{
		return _flags;
	}

	/**
	 * Get the style.
	 *
	 * @return The style or null.
	 */
	public DrawStyle getStyle()
	{
		return _context == null ? null : _context._style;
	}

	/**
	 * Sets the base position.<br>
	 * Doesn't set dirty as this method is used to set up the visual.
	 *
	 * @param pt     The point.
	 * @param bounds The absolute bounds to set or null.
	 */
	public void setAbsolutePosition(Point2D.Float pt, Rectangle2D.Float bounds)
	{
		setAbsolutePosition(pt.x, pt.y, bounds);
	}

	/**
	 * Sets the base position.<br>
	 * Doesn't set dirty as this method is used to set up the visual.
	 *
	 * @param x      The new X ordinate.
	 * @param y      The new Y ordinate.
	 * @param bounds The new absolute bounds or null.
	 */
	public void setAbsolutePosition(float x, float y, Rectangle2D.Float bounds)
	{
		if (_absolutePosition.x != x || _absolutePosition.y != y ||
				(bounds != null && !_absoluteBounds.equals(bounds)))
		{
			setModified();
			if (bounds == null)
			{
				_absoluteBounds.x += x - _absolutePosition.x;
				_absoluteBounds.y += y - _absolutePosition.y;
			}
			else
			{
				_absoluteBounds.setRect(bounds);
			}
			_absolutePosition.x = x;
			_absolutePosition.y = y;
		}
	}

	/**
	 * Gets the absolute base position.
	 *
	 * @return pt The base Point in absolute coordinates.
	 */
	public Point2D.Float getAbsolutePosition()
	{
		return new Point2D.Float(_absolutePosition.x, _absolutePosition.y);
	}

	/**
	 * Gets the absolute base position.
	 *
	 * @param pt The Point to set.
	 */
	public void getAbsolutePosition(Point2D.Float pt)
	{
		pt.x = _absolutePosition.x;
		pt.y = _absolutePosition.y;
	}

	/**
	 * Gets the primitive at the absolute position.
	 *
	 * @param x The x ordinate.
	 * @param y The y ordinate.
	 * @return The found primitive or null.
	 */
	public abstract DrawPrimitive getEditablePrimitiveAt(float x, float y);

	/**
	 * Get bounds of primitive according to style and alignment.
	 *
	 * @param g2        The graphics context.
	 * @param primitive The primitive to measure.
	 * @return The bounds or null.
	 */
	public Rectangle2D.Float getBoundsOfPrimitive(Graphics2D g2, DrawPrimitive primitive)
	{
		if (_absoluteBounds.width < 0)
			updateBounds(g2);

		if (primitive != null)
		{
			Dimension2DFloat dim = new Dimension2DFloat(_absoluteBounds);
			final Point2D.Float pt = getAlignmentOffset(g2, primitive, dim, new Point2D.Float());
			pt.x += _absolutePosition.x;
			pt.y += _absolutePosition.y;
			return primitive.getBounds2D(pt, g2);
		}
		return null;
	}

	/**
	 * Helper to get a aligned position according to alignment mode.
	 *
	 * @param g2        The graphics context to use for dimension calculations.
	 * @param primitive The primitive.
	 * @param dimension The dimension of the target box to align in.
	 * @param pt        The point to store the result.
	 * @return The aligned base point or null if {@link com.bw.graph.Alignment#Hidden}. Always same as pt if not null.
	 * @see DrawPrimitive#getAlignment()
	 */
	protected Point2D.Float getAlignmentOffset(Graphics2D g2, DrawPrimitive primitive,
											   Dimension2DFloat dimension, Point2D.Float pt)
	{
		switch (primitive.getAlignment())
		{
			case Left:
				pt.x = 0;
				break;
			case Center:
			{
				Dimension2DFloat dim = primitive.getDimension(g2);
				pt.x = (dimension._width - dim._width) / 2f - 1;
			}
			break;
			case Right:
			{
				Dimension2DFloat dim = primitive.getDimension(g2);
				pt.x = dimension._width - dim._width;
			}
			break;
			case Hidden:
				return null;
		}
		pt.y = 0;
		return pt;
	}

	/**
	 * Gets the absolute center position.
	 *
	 * @param g2 The graphics context to use for calculations.
	 * @return the position of the center in absolute coordinates.
	 */
	public Point2D.Float getCenterPosition(Graphics2D g2)
	{
		Rectangle2D.Float bounds = getAbsoluteBounds2D(g2);
		return new Point2D.Float((float) bounds.getCenterX(), (float) bounds.getCenterY());
	}

	/**
	 * Marks the visual for internal repaint.
	 * The visual itself can't trigger a repaint to the drawing surface, this needs to be done
	 * by caller. "invalidateBuffers" will force invalidation of any offscreen-buffers for the next draw cycle.
	 */
	public void invalidateBuffers()
	{
		_offscreenBuffersInvalid = true;
	}

	/**
	 * Writes the visual as SVG to the writer.
	 *
	 * @param sw The Writer to write to.
	 * @param g2 The graphics context - only for calculation. Must not be modified in any way.
	 */
	public abstract void toSVG(SVGWriter sw, Graphics2D g2);

	/**
	 * Gets the Id of the visual
	 *
	 * @return The id.
	 */
	public Object getId()
	{
		return _id;
	}

	/**
	 * Set the identification.
	 *
	 * @param id The identification Object.
	 */
	public void setId(Object id)
	{
		this._id = id;
	}

	/**
	 * Dispose all resources.
	 */
	public void dispose()
	{
		_id = null;
		_context = null;
	}

	/**
	 * Sets the preferred dimension.<br>
	 * If no preferred dimension is set, the dimension of the visual is calculated from its contents.
	 *
	 * @param dimension The dimension or null to reset to calculated dimension.
	 */
	public void setPreferredDimension(Dimension2DFloat dimension)
	{
		if (dimension == null)
		{
			if (this._dimension != null)
			{
				this._dimension = null;
				resetBounds();
				invalidateBuffers();
			}
		}
		else
			setPreferredDimension(dimension._width, dimension._height);
	}

	/**
	 * Sets the preferred dimension.
	 *
	 * @param width  The width.
	 * @param height The height.
	 */
	public void setPreferredDimension(float width, float height)
	{
		if (_dimension == null || _dimension._width != width || _dimension._height != height)
		{
			setModified();
			this._dimension = new Dimension2DFloat(width, height);
			resetBounds();
			invalidateBuffers();
		}
	}

	/**
	 * Gets the preferred dimension or null.
	 *
	 * @return The dimension or null.
	 */
	public Dimension2DFloat getPreferredDimension()
	{
		return _dimension;
	}

	/**
	 * Get the GraphConfiguration from current context.<br>
	 * Same as {@link #getDrawContext()}.configuration.
	 *
	 * @return the GraphConfiguration
	 */
	public GraphConfiguration getConfiguration()
	{
		return _context._configuration;
	}

	/**
	 * Get DrawContext
	 *
	 * @return The context.
	 */
	public DrawContext getDrawContext()
	{
		return _context;
	}


	/**
	 * Sets modified status.
	 */
	public void setModified()
	{
		setFlags(VisualFlags.MODIFIED);
	}

	/**
	 * Checks if the visual itself or the inner model was modified
	 *
	 * @return true if modified.
	 */
	public boolean isModified()
	{
		return isFlagSet(VisualFlags.MODIFIED);
	}

	@Override
	public String toString()
	{
		return String.valueOf(_id);
	}

	/**
	 * Gets the first primitive with the specified class oder super-class.
	 *
	 * @param primitiveClass The class to search for.
	 * @param <T>            The type of the class.
	 * @return The primitive or null.
	 */
	public <T extends DrawPrimitive> T getPrimitiveOf(Class<T> primitiveClass)
	{
		for (DrawPrimitive primitive : getPrimitives())
		{
			if (primitiveClass.isAssignableFrom(primitive.getClass()))
				return (T) primitive;
		}
		return null;
	}

	/**
	 * Gets the first primitive proxy with the specified class oder super-class.
	 *
	 * @param proxyClass The class to search for.
	 * @param <T>        The type of the class.
	 * @return The proxy or null.
	 */
	public <T extends EditorProxy> T getProxyOf(Class<T> proxyClass)
	{
		for (DrawPrimitive primitive : getPrimitives())
		{
			Object proxy = primitive.getUserData();
			if (proxy != null && proxyClass.isAssignableFrom(proxy.getClass()))
				return (T) proxy;
		}
		return null;
	}

	/**
	 * Provides list of all primitives.
	 *
	 * @return The list of primitives. Possibly empty but never null.
	 */
	public abstract List<DrawPrimitive> getPrimitives();

	/**
	 * Gets the display name.
	 * If {@link #_displayName} is null, the string representation of {@link #_id} is returned.
	 *
	 * @return The display name.
	 * @see #setDisplayName(String)
	 * @see #_displayName
	 */
	public String getDisplayName()
	{
		return _displayName == null ? String.valueOf(getId()) : _displayName;

	}

}

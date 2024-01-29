package com.bw.graph.primitive;

import com.bw.graph.Alignment;
import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.editor.EditorProxy;
import com.bw.graph.util.Dimension2DFloat;
import com.bw.graph.util.InsetsFloat;
import com.bw.graph.visual.Visual;
import com.bw.graph.visual.VisualFlags;
import com.bw.svg.SVGWriter;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

/**
 * A draw primitive.<br>
 * Used by Visuals to draw.
 */
public abstract class DrawPrimitive
{
	/**
	 * Style to use.
	 */
	protected final DrawStyle _style;

	private final Point2D.Float _relativePosition;
	private final Point2D.Float _tempPosition = new Point2D.Float();

	private int _flags = VisualFlags.ALWAYS;

	/**
	 * Assigned Visual. Will be set temporary during event handling.
	 * Not reliable during draw-calls.
	 */
	private Visual _visual;

	/**
	 *
	 */
	private Object _userData;

	/**
	 * The insets of the component.
	 */
	private InsetsFloat _insets = DEFAULT_INSERTS;

	/**
	 * Shared default insets instance.
	 */
	public static final InsetsFloat DEFAULT_INSERTS = new InsetsFloat(0, 0, 0, 0);


	/**
	 * Graph configuration to use.
	 */
	protected GraphConfiguration _config;

	/**
	 * The default alignment.
	 */
	// @TODO: Shall be configured from system defaults.
	public static Alignment DEFAULT_ALIGNMENT = Alignment.Left;

	/**
	 * The alignment of this primitive.
	 */
	protected Alignment _alignment;

	/**
	 * Creates a new Primitive.
	 *
	 * @param x      The relative x-position. Can also be negative.
	 * @param y      The relative y-position. Can also be negative.
	 * @param config The configuration to use.
	 * @param style  The local style.
	 * @param flags  The initial flags. @see {@link VisualFlags}
	 */
	protected DrawPrimitive(float x, float y, GraphConfiguration config, DrawStyle style, int flags)
	{
		Objects.requireNonNull(style);
		this._style = style;
		this._relativePosition = new Point2D.Float(x, y);
		this._config = config;
		this._flags = flags;
	}

	/**
	 * Sets visual.<br>
	 * The visual is only set during event handling or similar operations.
	 *
	 * @param v The visual or null.
	 */
	public void setVisual(Visual v)
	{
		_visual = v;
	}

	/**
	 * Gets the current assigned visual.<br>
	 * The visual is only reliable during event handling or similar operations.
	 *
	 * @return The visual or null.
	 */
	public Visual getVisual()
	{
		return _visual;
	}

	/**
	 * Sets a payload object.
	 *
	 * @param userData The data object or null.
	 */
	public void setUserData(Object userData)
	{
		this._userData = userData;
	}

	/**
	 * Gets the payload object.
	 *
	 * @return The data object or null.
	 */
	public Object getUserData()
	{
		return _userData;
	}


	/**
	 * Draws for given context.<br>
	 * This method calls {@link #drawIntern(Graphics2D)}
	 * with adapted position.
	 *
	 * @param g2 The graphics context
	 */
	public void draw(Graphics2D g2)
	{
		_tempPosition.x = _relativePosition.x + _insets._left;
		_tempPosition.y = _relativePosition.y + _insets._top;

		AffineTransform orgTransform = g2.getTransform();
		try
		{
			g2.translate(_tempPosition.x, _tempPosition.y);
			drawIntern(g2);
		}
		finally
		{
			g2.setTransform(orgTransform);
		}
	}

	/**
	 * Draws at given absolute position.
	 *
	 * @param g2 The graphics context, with (0,0) at final position.
	 */
	protected abstract void drawIntern(Graphics2D g2);


	/**
	 * Force a repaint.
	 * Needs to be implemented by affected primitives.
	 * Base implementation does nothing and doesn't need to be called from overrides.
	 */
	public void repaint()
	{
	}


	/**
	 * Get the local style if independent of parent.
	 *
	 * @return The style or null.
	 */
	public DrawStyle getStyle()
	{
		return _style;
	}

	/**
	 * Gets the relative position.
	 *
	 * @return The relative position.
	 */
	public Point2D.Float getRelativePosition()
	{
		return _relativePosition;
	}

	/**
	 * Gets the bounds of the primitive, including space for insets.
	 *
	 * @param basePosition The absolute base position
	 * @param graphics     The graphics context to use for calculations.
	 * @return The bounds as rectangle.
	 */
	public Rectangle2D.Float getBounds2D(Point2D.Float basePosition, Graphics2D graphics)
	{
		return getBounds2D(basePosition.x, basePosition.y, graphics);
	}

	/**
	 * Gets the bounds of the primitive, including space for insets.
	 *
	 * @param basePositionX The absolute base X position.
	 * @param basePositionY The absolute base Y position.
	 * @param graphics      The graphics context to use for calculations.
	 * @return The bounds as rectangle.
	 */
	public Rectangle2D.Float getBounds2D(float basePositionX, float basePositionY, Graphics2D graphics)
	{
		final Dimension2DFloat dim = getDimension(graphics);
		return new Rectangle2D.Float(
				basePositionX + _relativePosition.x,
				basePositionY + _relativePosition.y, dim._width, dim._height);
	}

	/**
	 * Get the alignment.
	 *
	 * @return The alignment-mode. Never null.
	 */
	public Alignment getAlignment()
	{
		return (_alignment == null) ? DEFAULT_ALIGNMENT : _alignment;
	}

	/**
	 * Set the alignment.
	 *
	 * @param alignment The alignment-mode. Can be null.
	 */
	public void setAlignment(Alignment alignment)
	{
		this._alignment = alignment;
	}

	/**
	 * Gets the bounds of the primitive, including insets.
	 *
	 * @param graphics The graphics context to use for calculations.
	 * @return The bounds as rectangle.
	 */
	public Dimension2DFloat getDimension(Graphics2D graphics)
	{
		Dimension2DFloat dim = getInnerDimension(graphics);
		dim._width += _insets._left + _insets._right;
		dim._height += _insets._top + _insets._bottom;

		return dim;
	}


	/**
	 * Gets the bounds of the primitive. Without insets.
	 *
	 * @param graphics The graphics context to use for calculations.
	 * @return The dimension.
	 */
	protected abstract Dimension2DFloat getInnerDimension(Graphics2D graphics);

	/**
	 * Adds the primitive as SVG element to the string builder.
	 *
	 * @param sw       The Writer to write to.
	 * @param g2       The Graphic context - only for calculations.
	 * @param position Target position.
	 */
	public void toSVG(SVGWriter sw, Graphics2D g2,
					  Point2D.Float position)
	{
		_tempPosition.x = _relativePosition.x + _insets._left;
		_tempPosition.y = _relativePosition.y + _insets._top;

		if (position != null)
		{
			_tempPosition.x += position.x;
			_tempPosition.y += position.y;
		}

		toSVGIntern(sw, g2, _tempPosition);

	}

	/**
	 * Internal implementation from inheritances.
	 *
	 * @param sw  The Writer to write to.
	 * @param g2  The Graphic context - only for calculations.
	 * @param pos The calculated position (including the relative position).
	 */
	protected abstract void toSVGIntern(SVGWriter sw, Graphics2D g2, Point2D.Float pos);

	/**
	 * Sets new insets.
	 *
	 * @param top    from the top.
	 * @param left   from the left.
	 * @param bottom from the bottom.
	 * @param right  from the right.
	 */
	public void setInsets(float top, float left, float bottom, float right)
	{
		if (_insets == DEFAULT_INSERTS)
			_insets = new InsetsFloat(top, left, bottom, right);
		else
		{
			_insets._top = top;
			_insets._left = left;
			_insets._bottom = bottom;
			_insets._right = right;
		}
	}

	/**
	 * Sets insets.
	 *
	 * @param insets The new insets or null to reset to 0,0,0,0.
	 */
	public void setInsets(InsetsFloat insets)
	{
		if (insets != null)
		{
			if (this._insets == DEFAULT_INSERTS)
				this._insets = new InsetsFloat(insets._top, insets._left, insets._bottom, insets._right);
			else
			{
				this._insets._top = insets._top;
				this._insets._left = insets._left;
				this._insets._bottom = insets._bottom;
				this._insets._right = insets._right;
			}
		}
		else
		{
			this._insets = DEFAULT_INSERTS;
		}
	}

	/**
	 * Gets insets.
	 *
	 * @return The insets, never null.
	 */
	public InsetsFloat getInsets()
	{
		return _insets;
	}

	/**
	 * Release resource.
	 */
	public void dispose()
	{
		_visual = null;
	}

	/**
	 * Sets Bit Flags.
	 *
	 * @param flags Flag bits to add.
	 */
	public void setFlags(int flags)
	{
		this._flags |= flags;
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
		this._flags &= ~flags;
	}

	/**
	 * Gets the current flags.
	 *
	 * @return Bitwise combination of flags.
	 */
	public int getFlags()
	{
		return _flags;
	}

}
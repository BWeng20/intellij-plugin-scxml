package com.bw.graph;

import com.bw.svg.SVGWriter;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.logging.Logger;

/**
 * Component to draw the graph.
 */
public class GraphPane extends JComponent
{
	/**
	 * Graph configuration
	 */
	GraphConfiguration configuration = new GraphConfiguration();

	/**
	 * Logger of this class.
	 */
	static final Logger log = Logger.getLogger(GraphPane.class.getName());

	/**
	 * Listens to clicks and drags on visuals.
	 */
	protected MouseAdapter mouseAdapter = new MouseAdapter()
	{
		/** The visual that is currently dragged or null. */
		private Visual draggingVisual;

		/** The last coordinate of a drag-event. */
		private Point lastDragPoint = new Point(0, 0);

		@Override
		public void mouseClicked(MouseEvent e)
		{
			setSelectedVisual(getVisualAt(e.getX(), e.getY()));
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			draggingVisual = getVisualAt(lastDragPoint.x = e.getX(), lastDragPoint.y = e.getY());

			if (draggingVisual != null)
			{
				visuals.remove(draggingVisual);
				visuals.add(draggingVisual);
			}

			SwingUtilities.convertPointToScreen(lastDragPoint, GraphPane.this);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			draggingVisual = null;
			lastDragPoint.x = lastDragPoint.y = 0;
			SwingUtilities.convertPointToScreen(lastDragPoint, GraphPane.this);
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			// Work on global coordinates as the component we are dragging
			// on will change its location.
			Point mp = e.getPoint();
			SwingUtilities.convertPointToScreen(mp, GraphPane.this);

			final int xd = mp.x - lastDragPoint.x;
			final int yd = mp.y - lastDragPoint.y;

			if (draggingVisual != null)
			{
				draggingVisual.moveBy(xd, yd);
				revalidate();
				repaint();
			}
			else
			{
				JViewport viewPort = (JViewport) SwingUtilities.getAncestorOfClass(JViewport.class, GraphPane.this);
				if (viewPort != null)
				{
					Point p = viewPort.getViewPosition();
					p.x -= xd;
					p.y -= yd;
					if (p.x < 0)
						p.x = 0;
					if (p.y < 0)
						p.y = 0;
					viewPort.setViewPosition(p);
				}
			}
			lastDragPoint.setLocation(mp);
		}
	};

	/**
	 * Creates a new graph pane.
	 */
	public GraphPane()
	{
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
	}

	/**
	 * Gets the visual at the coordinates (x,y).
	 *
	 * @param x The component local X-ordinate.
	 * @param y The component local X-ordinate.
	 * @return The found visual or null.
	 */
	protected Visual getVisualAt(float x, float y)
	{
		x -= offsetX;
		y -= offsetY;
		for (var it = visuals.listIterator(visuals.size()); it.hasPrevious(); )
		{
			final Visual v = it.previous();
			if (v.containsPoint(x, y))
			{
				return v;
			}
		}
		return null;
	}

	/**
	 * List of visuals.
	 */
	protected LinkedList<Visual> visuals = new LinkedList<>();

	/**
	 * Drawing X-offset.
	 */
	protected float offsetX = 0;

	/**
	 * Drawing Y-offset.
	 */
	protected float offsetY = 0;

	/**
	 * Last selected visual or null.
	 */
	protected Visual selectedVisual;

	@Override
	protected void paintComponent(Graphics g)
	{
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(offsetX, offsetY);

		if (configuration.antialiasing)
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		try
		{
			if (isOpaque())
			{
				g.setColor(getBackground());
				g.fillRect(0, 0, getWidth(), getHeight());
			}

			for (Visual v : visuals)
				v.draw(g2);
		}
		finally
		{
			g2.dispose();
		}
	}

	/**
	 * Created SVG from the graph.
	 *
	 * @return The generated SVG source code.
	 */
	public String toSVG()
	{
		Graphics2D g2 = (Graphics2D) getGraphics();
		StringWriter ssw = new StringWriter();
		SVGWriter sw = new SVGWriter(ssw);

		Rectangle2D.Float bounds = getBounds2D();
		sw.startSVG(bounds, null);
		for (Visual v : visuals)
			v.toSVG(sw, g2);
		sw.endSVG();
		return ssw.getBuffer()
				  .toString();
	}


	/**
	 * Adds a new visual to the graph.
	 *
	 * @param v The visual.
	 */
	public void addVisual(Visual v)
	{
		if (v != null)
		{
			v.resetBounds();
			visuals.add(v);
			repaint();
		}

		log.warning(toSVG());
	}

	/**
	 * Sets the selected visual.
	 *
	 * @param visual The new visual or null to deselect.
	 */
	public void setSelectedVisual(Visual visual)
	{
		boolean triggerRepaint = false;

		if (selectedVisual != null && selectedVisual != visual && selectedVisual.isHighlighted())
		{
			selectedVisual.setHighlighted(false);
			triggerRepaint = true;
		}
		selectedVisual = visual;
		if (selectedVisual != null && !selectedVisual.isHighlighted())
		{
			visual.setHighlighted(true);
			triggerRepaint = true;
		}
		if (selectedVisual != null && visuals.indexOf(selectedVisual) != (visuals.size() - 1))
		{
			visuals.remove(selectedVisual);
			visuals.add(selectedVisual);
			triggerRepaint = true;
		}
		if (triggerRepaint)
		{
			repaint();
		}
	}

	/**
	 * Release any resources
	 */
	public void dispose()
	{
		selectedVisual = null;
		visuals.clear();
		removeMouseListener(mouseAdapter);
		removeMouseMotionListener(mouseAdapter);
		mouseAdapter = null;
	}

	@Override
	public Dimension getPreferredSize()
	{
		Dimension d;
		if (isPreferredSizeSet())
		{
			d = super.getPreferredSize();
		}
		else
		{
			Rectangle2D.Float bounds = getBounds2D();
			d = new Dimension((int) Math.ceil(bounds.width), (int) Math.ceil(bounds.height));
		}
		return d;
	}

	/**
	 * Calculate the bounds of the graph.
	 *
	 * @return The bounds, containing all elements.
	 */
	public Rectangle2D.Float getBounds2D()
	{
		Rectangle2D.Float bounds = new Rectangle2D.Float(0, 0, 0, 0);
		float x2 = 0;
		float y2 = 0;
		float t2;
		final Graphics2D g2 = (Graphics2D) getGraphics();
		for (Visual visual : visuals)
		{
			Rectangle2D.Float visualBounds = visual.getBounds2D(g2);
			if (visualBounds != null)
			{
				if (bounds.x > visualBounds.x)
					bounds.x = visualBounds.x;
				if (bounds.y > visualBounds.y)
					bounds.y = visualBounds.y;
				t2 = visualBounds.x + visualBounds.width;
				if (x2 < t2)
					x2 = t2;
				t2 = visualBounds.y + visualBounds.height;
				if (y2 < t2)
					y2 = t2;
			}
		}
		bounds.height = y2 - bounds.y + 5;
		bounds.width = x2 - bounds.x + 5;

		return bounds;
	}

	/**
	 * Remove all visuals.
	 */
	public void removeAllVisuals()
	{
		visuals.clear();
		repaint();
	}

	/**
	 * Gets the graph configuration.
	 *
	 * @return The getGraphConfiguration, never null.
	 */
	public GraphConfiguration getGraphConfiguration()
	{
		return configuration;
	}
}

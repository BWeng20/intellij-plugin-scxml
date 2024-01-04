package com.bw.graph;

import javax.swing.JComponent;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Component to draw the graph.
 */
public class GraphPane extends JComponent
{
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

		/** The last X ordinate of a drag-event. */
		private int lastDragX;
		/** The last Y ordinate of a drag-event. */
		private int lastDragY;

		@Override
		public void mouseClicked(MouseEvent e)
		{
			setSelectedVisual(getVisualAt(e.getX(), e.getY()));
		}


		@Override
		public void mousePressed(MouseEvent e)
		{
			draggingVisual = getVisualAt(lastDragX = e.getX(), lastDragY = e.getY());
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			draggingVisual = null;
			lastDragX = lastDragY = 0;
		}

		@Override
		public void mouseWheelMoved(MouseWheelEvent e)
		{
		}

		@Override
		public void mouseDragged(MouseEvent e)
		{
			if (draggingVisual != null)
			{
				draggingVisual.moveBy(e.getX() - lastDragX, e.getY() - lastDragY);
				lastDragX = e.getX();
				lastDragY = e.getY();
				revalidate();
				repaint();
			}
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
		for (Visual v : visuals)
		{
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
	protected List<Visual> visuals = new LinkedList<>();

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
		if (triggerRepaint)
			repaint();
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
			Rectangle2D.Float bounds = new Rectangle2D.Float(0, 0, 0, 0);
			float x2 = 0;
			float y2 = 0;
			float t2;
			for (Visual visual : visuals)
			{
				Rectangle2D.Float visualBounds = visual.getBounds2D();
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
			d = new Dimension((int) Math.ceil(bounds.width), (int) Math.ceil(bounds.height));
		}
		return d;
	}

	/**
	 * Remove all visuals.
	 */
	public void removeAllVisuals()
	{
		visuals.clear();
		repaint();
	}
}

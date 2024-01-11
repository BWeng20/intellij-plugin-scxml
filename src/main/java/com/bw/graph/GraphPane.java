package com.bw.graph;

import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.Visual;
import com.bw.svg.SVGWriter;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.InputEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.awt.geom.Rectangle2D;
import java.io.StringWriter;
import java.util.List;
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
				model.moveVisualToTop(draggingVisual);
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
		public void mouseWheelMoved(MouseWheelEvent we)
		{
			if (configuration.zoomByMetaMouseWheelEnabled)
			{
				int wheel = we.getWheelRotation();
				if (wheel != 0)
				{
					int mod = we.getModifiersEx();
					if ((mod & InputEvent.META_DOWN_MASK) != 0 || (mod & InputEvent.CTRL_DOWN_MASK) != 0)
					{
						float scale = configuration.scale - 0.1f * wheel;
						if (scale >= 0.1)
						{
							configuration.scale = scale;
							SwingUtilities.invokeLater(() -> {
								if (configuration.doubleBuffered)
									model.getVisuals().forEach(Visual::repaint);
								revalidate();
								repaint();
							});
						}
					}
				}
			}
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
				draggingVisual.moveBy(xd / configuration.scale, yd / configuration.scale);
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
		setModel(new VisualModel());
		addMouseListener(mouseAdapter);
		addMouseMotionListener(mouseAdapter);
		addMouseWheelListener(mouseAdapter);
	}

	/**
	 * Gets the visual at the coordinates (x,y).
	 *
	 * @param x The component local X-ordinate (unscaled).
	 * @param y The component local X-ordinate (unscaled).
	 * @return The found visual or null.
	 */
	protected Visual getVisualAt(float x, float y)
	{
		x -= offsetX;
		y -= offsetY;

		x /= configuration.scale;
		y /= configuration.scale;

		var visuals = model.getVisuals();
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
	 * The top level model
	 */
	protected VisualModel model;

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
				g2.setColor(getBackground());
				g2.fillRect(0, 0, getWidth(), getHeight());
			}

			g2.scale(configuration.scale, configuration.scale);

			for (EdgeVisual e : model.getEdges())
				e.draw(g2);

			for (Visual v : model.getVisuals())
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
		for (Visual v : model.getVisuals())
			v.toSVG(sw, g2);
		sw.endSVG();
		return ssw.getBuffer()
				  .toString();
	}

	/**
	 * Sets the model.
	 *
	 * @param model The new model. Can be null.
	 */
	public void setModel(VisualModel model)
	{
		selectedVisual = null;
		if (this.model != null)
		{
			// Will also remove our listener.
			this.model.dispose();
		}
		if (model == null)
			model = new VisualModel();
		this.model = model;
		model.addListener(this::repaint);
		repaint();
	}

	/**
	 * Gets the model.
	 *
	 * @return The model. Never null.
	 */
	public VisualModel getModel()
	{
		return model;
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
		List<Visual> visuals = model.getVisuals();
		if (selectedVisual != null && visuals.indexOf(selectedVisual) != (visuals.size() - 1))
		{
			model.moveVisualToTop(selectedVisual);
			// Repaint will be triggered my model listener
			triggerRepaint = false;
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
		model.dispose();
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
	 * @return The scaled bounds, containing all elements.
	 */
	public Rectangle2D.Float getBounds2D()
	{
		Rectangle2D.Float bounds = new Rectangle2D.Float(0, 0, 0, 0);
		float x2 = 0;
		float y2 = 0;
		float t2;
		final Graphics2D g2 = (Graphics2D) getGraphics();
		for (Visual visual : model.getVisuals())
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
		bounds.x *= configuration.scale;
		bounds.y *= configuration.scale;
		bounds.height = (configuration.scale * y2) - bounds.y + 5;
		bounds.width = (configuration.scale * x2) - bounds.x + 5;

		return bounds;
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

package com.bw.graph.editor;

import com.bw.graph.GraphConfiguration;
import com.bw.graph.VisualModel;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.visual.EdgeVisual;
import com.bw.graph.visual.Visual;
import com.bw.svg.SVGWriter;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Component to draw the graph.
 */
public class GraphPane extends JComponent
{
	/**
	 * Graph configuration
	 */
	private GraphConfiguration configuration = new GraphConfiguration();

	/**
	 * Listeners.
	 */
	private final LinkedList<InteractionListener> listeners = new LinkedList<>();

	/**
	 * Queue of parent states we had entered.
	 */
	protected LinkedList<Visual> parents = new LinkedList<>();

	/**
	 * Milliseconds needed of last paint cycle.
	 */
	private long lastPaintMS;

	/**
	 * If true {@link #lastPaintMS} is shown on screen for debugging.
	 */
	private boolean showDrawSpeed = true;

	/**
	 * Listens to clicks and drags on visuals.
	 */
	protected MouseAdapter mouseAdapter = new MouseAdapter()
	{
		/** The visual that is currently dragged or null. */
		private Visual draggingVisual;

		/** The last coordinate of a drag-event. */
		private final Point lastDragPoint = new Point(0, 0);

		@Override
		public void mouseClicked(MouseEvent e)
		{
			int x = e.getX();
			int y = e.getY();

			Visual clicked = getVisualAt(x,y);
			if (clicked != null && e.getClickCount() > 1)
			{
				x -= offsetX;
				y -= offsetY;

				x /= configuration.scale;
				y /= configuration.scale;

				Rectangle2D.Float sunModelBox = clicked.getSubModelBounds(null);
				if (sunModelBox != null && sunModelBox.contains(x,y))
				{
					parents.add(clicked);
					clicked.setHighlighted(false);
					setModel(clicked.getSubModel());
					fireHierarchyChanged();
				} else {
					setSelectedPrimitive( clicked.getEditablePrimitiveAt(x,y) );
				}
			}
		}

		@Override
		public void mousePressed(MouseEvent e)
		{
			draggingVisual = getVisualAt(lastDragPoint.x = e.getX(), lastDragPoint.y = e.getY());
			setSelectedVisual(draggingVisual);
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
					if ((mod & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK)) != 0)
					{
						float scale = configuration.scale - 0.1f * wheel;
						if (scale >= 0.1)
						{
							configuration.scale = scale;
							SwingUtilities.invokeLater(() ->
							{
								if (configuration.buffered)
									model.repaint();
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

	protected DrawPrimitive selectedPrimitive;


	@Override
	protected void paintComponent(Graphics g)
	{
		final long start = System.currentTimeMillis();
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(offsetX, offsetY);

		if (configuration.antialiasing)
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		try
		{
			if (isOpaque())
			{
				g2.setPaint(configuration.graphBackground == null ? getBackground() : configuration.graphBackground);
				g2.fillRect(0, 0, getWidth(), getHeight());
			}

			g2.scale(configuration.scale, configuration.scale);

			model.draw(g2);
			if ( selectedPrimitive != null )
				drawPrimitiveCursor(g2, selectedPrimitive);

		}
		finally
		{
			g2.dispose();

			final long end = System.currentTimeMillis();
			lastPaintMS = end - start;

			if (showDrawSpeed)
			{
				g.setColor(Color.BLACK);
				g.setFont(getFont());
				char[] text = (Long.toString(lastPaintMS) + "ms").toCharArray();
				g.drawChars(text, 0, text.length, 0, 20);
			}
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
		if (model != this.model)
		{
			boolean fireHierarchy = false;
			while (!parents.isEmpty())
			{
				if (parents.peekLast().getSubModel() == model)
					break;
				parents.removeLast();
				fireHierarchy = true;
			}

			Visual oldSelected = selectedVisual;
			selectedVisual = null;
			selectedPrimitive = null;
			if (this.model != null)
			{
				this.model.removeListener(this::repaint);
			}
			if (model == null)
				model = new VisualModel();
			this.model = model;
			model.addListener(this::repaint);

			if (oldSelected != null)
			{
				if (oldSelected.isHighlighted())
					oldSelected.setHighlighted(false);
				fireVisualDeselected(oldSelected);
			}

			if (fireHierarchy)
				fireHierarchyChanged();

			// Force repaint.
			model.repaint();
			repaint();
		}
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
		cancelEdit();
		setSelectedPrimitive(null);

		boolean triggerRepaint = false;

		Visual oldSelected = selectedVisual;
		selectedVisual = visual;

		if (oldSelected != null && oldSelected != selectedVisual)
		{
			if (oldSelected.isHighlighted())
			{
				oldSelected.setHighlighted(false);
				triggerRepaint = true;
			}
		}

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

		if (oldSelected != null && oldSelected != selectedVisual)
		{
			if (selectedVisual == null)
				fireVisualDeselected(oldSelected);
			else
				fireVisualSelected();
		}

		if (triggerRepaint)
		{
			repaint();
		}
	}

	public void setSelectedPrimitive( DrawPrimitive p) {

		if ( p != selectedPrimitive ) {

			cancelEdit();

			Graphics2D g2 = (Graphics2D)getGraphics();
			try
			{
				g2.translate(offsetX, offsetY);
				g2.scale(configuration.scale, configuration.scale);

				if (selectedPrimitive != null)
				{
					drawPrimitiveCursor(g2, selectedPrimitive);
				}
				selectedPrimitive = p;
				if (selectedPrimitive != null)
				{
					drawPrimitiveCursor(g2, selectedPrimitive);
				}
			}finally
			{
				g2.dispose();
			}
		}

	}

	protected void drawPrimitiveCursor(Graphics2D g2, DrawPrimitive primitive) {
		Visual v = primitive.getVisual();

		g2.setStroke(new BasicStroke(3));
		g2.setColor(Color.BLUE);
		g2.setXORMode(Color.RED);
		Point.Float pt = v.getPosition();

		Rectangle2D.Float rt = v.getBoundsOfPrimitive(g2, primitive, v.getStyle() );
		if ( rt != null )
		{
			rt.x -= 2;
			rt.y -= 2;
			rt.width += 4;
			rt.height += 4;
			g2.draw(rt);
		}
	}

	/**
	 * Release any resources
	 */
	public void dispose()
	{
		selectedVisual = null;
		model = null;
		removeMouseListener(mouseAdapter);
		removeMouseMotionListener(mouseAdapter);
		listeners.clear();
		parents.clear();
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
		Rectangle2D.Float bounds = model.getBounds2D((Graphics2D) getGraphics());
		bounds.x *= configuration.scale;
		bounds.y *= configuration.scale;
		bounds.height = 5 + bounds.height * configuration.scale;
		bounds.width = 5 + bounds.width * configuration.scale;

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

	/**
	 * Adds a new interaction listener.
	 *
	 * @param listener The listener to add.
	 */
	public void addInteractionListener(InteractionListener listener)
	{
		this.listeners.remove(listener);
		this.listeners.add(listener);
	}

	/**
	 * Removes an interaction listener.
	 *
	 * @param listener The listener to add.
	 */
	public void removeInteractionListener(InteractionListener listener)
	{
		this.listeners.remove(listener);
	}

	/**
	 * Calls {@link InteractionListener#selected(Visual)} on all listeners.
	 */
	protected void fireVisualSelected()
	{
		new ArrayList<>(listeners).forEach(listener -> listener.selected(selectedVisual));
	}

	/**
	 * Calls {@link InteractionListener#deselected(Visual)} on all listeners.
	 *
	 * @param oldSelected The de-selected visual.
	 */
	protected void fireVisualDeselected(Visual oldSelected)
	{
		new ArrayList<>(listeners).forEach(listener -> listener.deselected(oldSelected));
	}

	/**
	 * Calls hierarchyChanged on all listeners.
	 */
	protected void fireHierarchyChanged()
	{
		new ArrayList<>(listeners).forEach(InteractionListener::hierarchyChanged);
	}

	/**
	 * Get current hierarchy
	 *
	 * @return The chain of parents the editor entered.
	 */
	public List<Visual> getHierarchy()
	{
		return Collections.unmodifiableList(parents);
	}


	protected void cancelEdit() {

	}
}

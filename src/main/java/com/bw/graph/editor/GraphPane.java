package com.bw.graph.editor;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.VisualModel;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.visual.Visual;
import com.bw.graph.visual.VisualFlags;
import com.bw.svg.SVGAttribute;
import com.bw.svg.SVGWriter;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.*;
import java.awt.event.*;
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
	 * Listen to key events.
	 */
	protected KeyAdapter keyAdapter = new KeyAdapter()
	{
		@Override
		public void keyPressed(KeyEvent e)
		{
			System.err.println("Pressed key " + e);
		}
	};

	/**
	 * Focus listener used to control editors.
	 */
	protected FocusListener editorFocusAdapter = new FocusAdapter()
	{
		@Override
		public void focusLost(FocusEvent e)
		{
			cancelEdit();
		}
	};


	/**
	 * Key adapter, used to control editors.
	 */
	protected KeyAdapter editorKeyAdapter = new KeyAdapter()
	{
		@Override
		public void keyTyped(KeyEvent e)
		{
			switch (e.getKeyChar())
			{
				case KeyEvent.VK_ESCAPE:
				{
					cancelEdit();
				}
				break;
				case KeyEvent.VK_ENTER:
				{
					endEdit();
				}
				break;
			}
		}
	};

	/**
	 * The visual that is currently dragged or null.
	 */
	private Visual draggingVisual;

	/**
	 * The last coordinate of a drag-event.
	 */
	private final Point lastDragPoint = new Point(0, 0);

	/**
	 * Current visual the mouse is over.
	 */
	private Visual mouseOverVisual;

	/**
	 * Listens to clicks and drags on visuals.
	 */
	protected MouseListener mouseHandler = new MouseListener()
	{
		@Override
		public void mouseEntered(MouseEvent e)
		{
		}

		@Override
		public void mouseExited(MouseEvent e)
		{

		}

		@Override
		public void mouseClicked(MouseEvent e)
		{
			float x = e.getX();
			float y = e.getY();

			Visual clicked = getVisualAt(x, y);
			if (clicked != null && e.getClickCount() > 1)
			{
				x -= offsetX;
				y -= offsetY;

				x /= configuration.scale;
				y /= configuration.scale;

				DrawPrimitive editablePrimitive = clicked.getEditablePrimitiveAt(x, y);
				if (editablePrimitive == null)
				{
					ModelPrimitive modelPrimitive = clicked.getPrimitiveOf(ModelPrimitive.class);
					if (modelPrimitive != null)
					{
						Rectangle2D.Float subModelBox = clicked.getBoundsOfPrimitive(null, modelPrimitive);
						if (subModelBox != null && subModelBox.contains(x, y))
						{
							parents.add(clicked);
							setModel(modelPrimitive.getSubModel());
							fireHierarchyChanged();
							return;
						}
					}
				}
				setSelectedPrimitive(editablePrimitive);
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
			boolean fireDragged = draggingVisual != null;
			draggingVisual = null;
			lastDragPoint.x = lastDragPoint.y = 0;
			if (fireDragged)
				fireMouseDragging(null);
		}

	};

	/**
	 * Handles mouse wheel.
	 */
	protected MouseWheelListener mouseWheelHandler = new MouseWheelListener()
	{
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
								cancelEdit();
								revalidate();
								repaint();
							});
						}
					}
				}
			}
		}
	};

	/**
	 * Listens to clicks and drags on visuals.
	 */
	protected MouseMotionListener mouseMotionHandler = new MouseMotionListener()
	{
		@Override
		public void mouseMoved(MouseEvent e)
		{
			Visual over = getVisualAt(e.getX(), e.getY());
			if (mouseOverVisual != over)
			{
				Rectangle2D.Float update = null;
				if (mouseOverVisual != null)
				{
					update = mouseOverVisual.getAbsoluteBounds2D(null);
				}

				mouseOverVisual = over;
				fireMouseOver(mouseOverVisual);

				if (mouseOverVisual != null)
				{
					Rectangle2D.Float rt = mouseOverVisual.getAbsoluteBounds2D(null);
					if (update == null)
						update = rt;
					else
						Rectangle2D.union(update, rt, update);
				}
				if (update != null)
				{
					update.x -= 2;
					update.y -= 2;
					update.width += 2 * 2;
					update.height += 2 * 2;
					update.height += 2 * 2;
					repaint(update.getBounds());
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
				fireMouseDragging(draggingVisual);
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
		setLayout(null);
		setModel(new VisualModel("none"));
		addMouseListener(mouseHandler);
		addMouseMotionListener(mouseMotionHandler);
		addMouseWheelListener(mouseWheelHandler);
		addKeyListener(keyAdapter);
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

	/**
	 * The current selected primitive.
	 */
	protected DrawPrimitive selectedPrimitive;

	/**
	 * The current active editor.
	 */
	protected JComponent selectedPrimitiveEditor;

	/**
	 * The proxy of the current active editor.
	 */
	protected EditorProxy selectedPrimitiveEditorProxy;


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
			if (selectedPrimitive != null && selectedPrimitiveEditor == null)
				drawPrimitiveCursor(g2, selectedPrimitive);

		}
		finally
		{
			g2.dispose();

			final long end = System.currentTimeMillis();
			lastPaintMS = end - start;

			if (showDrawSpeed)
			{
				g.setColor(getForeground());
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
		sw.precisionFactor = configuration.precisionFactor;

		Rectangle2D.Float bounds = getBounds2D();
		sw.startSVG(bounds);

		Paint bg = configuration.graphBackground == null ? getBackground() : configuration.graphBackground;
		if (bg != null)
		{
			sw.startStyle();
			sw.writeAttribute(SVGAttribute.BackgroundColor, bg);
			sw.endStyle();
		}
		if (model.name != null)
		{
			sw.startElement("title");
			sw.startContent();
			sw.writeEscaped(model.name);
			sw.endElement();
		}
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
			cancelEdit();

			boolean fireHierarchy = false;
			while (!parents.isEmpty())
			{
				if (ModelPrimitive.getSubModel(parents.peekLast()) == model)
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
				model = new VisualModel("none");
			this.model = model;
			model.addListener(this::repaint);

			if (oldSelected != null)
			{
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
			if (oldSelected.isFlagSet(VisualFlags.SELECTED))
			{
				oldSelected.clearFlags(VisualFlags.SELECTED);
				triggerRepaint = true;
			}
		}

		if (selectedVisual != null && !selectedVisual.isFlagSet(VisualFlags.SELECTED))
		{
			visual.setFlags(VisualFlags.SELECTED);
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

	/**
	 * Sets the current selected primitive.
	 *
	 * @param p The primitive
	 */
	public void setSelectedPrimitive(DrawPrimitive p)
	{
		if (p != selectedPrimitive)
		{
			cancelEdit();
			Graphics2D g2 = (Graphics2D) getGraphics();
			try
			{
				g2.translate(offsetX, offsetY);
				g2.scale(configuration.scale, configuration.scale);

				if (selectedPrimitive != null && selectedPrimitiveEditor == null)
				{
					drawPrimitiveCursor(g2, selectedPrimitive);
				}
				selectedPrimitive = p;
				startEdit(g2);
			}
			finally
			{
				g2.dispose();
			}
		}

	}

	/**
	 * Draw the cursor for the current primitive in XOR mode.
	 *
	 * @param g2        Graphics to use.
	 * @param primitive thr primitive.
	 */
	protected void drawPrimitiveCursor(Graphics2D g2, DrawPrimitive primitive)
	{
		Visual v = primitive.getVisual();

		g2.setStroke(new BasicStroke(3));
		g2.setColor(Color.BLUE);
		g2.setXORMode(Color.RED);

		Rectangle2D.Float rt = v.getBoundsOfPrimitive(g2, primitive);
		if (rt != null)
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
		removeMouseListener(mouseHandler);
		removeMouseMotionListener(mouseMotionHandler);
		removeMouseWheelListener(mouseWheelHandler);
		listeners.clear();
		parents.clear();
		mouseHandler = null;
		mouseMotionHandler = null;
		mouseWheelHandler = null;
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
	 * Calls mouseDragging on all listeners.
	 *
	 * @param visual The visual that is dragged or null.
	 */
	protected void fireMouseDragging(Visual visual)
	{
		new ArrayList<>(listeners).forEach(i -> i.mouseDragging(visual));
	}

	/**
	 * Calls mouseOver on all listeners.
	 *
	 * @param visual The visual the mouse is over or null.
	 */
	protected void fireMouseOver(Visual visual)
	{
		new ArrayList<>(listeners).forEach(i -> i.mouseOver(visual));
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

	/**
	 * Start edit of a primitive. There can be only one active edit.
	 *
	 * @param g2 The graphics context to use.
	 */
	protected void startEdit(Graphics2D g2)
	{
		cancelEdit();
		if (selectedPrimitive != null)
		{
			Object userData = selectedPrimitive.getUserData();

			if (userData instanceof EditorProxy editorProxy)
			{
				selectedPrimitiveEditorProxy = editorProxy;
				Visual v = selectedPrimitive.getVisual();
				if (selectedPrimitiveEditor != null)
				{
					// This should not happen...
					System.err.println("Previous Editor still active during new selection, should already by removed.");
					removePrimitiveEditor();
				}
				selectedPrimitiveEditor = selectedPrimitiveEditorProxy.getEditor(selectedPrimitive);
				Rectangle2D.Float rt = v.getBoundsOfPrimitive(g2, selectedPrimitive);

				final float scale = configuration.scale;
				rt.x += offsetX;
				rt.y += offsetY;
				rt.x *= scale;
				rt.y *= scale;
				rt.width *= scale;
				rt.height *= scale;

				Font font;
				FontMetrics fontMetrics;
				DrawStyle style = selectedPrimitive.getStyle();
				if (style.fontMetrics != null)
				{
					font = style.font;
					fontMetrics = style.fontMetrics;
				}
				else
				{
					font = getFont();
					fontMetrics = getFontMetrics(font);
				}
				font = font.deriveFont((float) (int) (0.5 + font.getSize() * configuration.scale));
				selectedPrimitiveEditor.setFont(font);
				Dimension d = selectedPrimitiveEditor.getPreferredSize();

				float minWidth = fontMetrics.charWidth('X') * 20 * configuration.scale;
				d.width = (int) (0.5 + Math.max(minWidth, d.width));

				rt.x += (rt.width - d.width) / 2f;
				rt.y += (rt.height - d.height) / 2f;
				rt.width = d.width;
				rt.height = d.height;

				selectedPrimitiveEditor.setBounds(rt.getBounds());
				selectedPrimitiveEditor.addKeyListener(editorKeyAdapter);
				selectedPrimitiveEditor.addFocusListener(editorFocusAdapter);
				add(selectedPrimitiveEditor);
				selectedPrimitiveEditor.requestFocus();
			}
			else
				drawPrimitiveCursor(g2, selectedPrimitive);
		}
	}

	/**
	 * Cancel any active editor.
	 *
	 * @see EditorProxy#cancelEdit(DrawPrimitive)
	 */
	protected void cancelEdit()
	{
		if (selectedPrimitiveEditor != null)
		{
			removePrimitiveEditor();
			if (selectedPrimitiveEditorProxy != null)
			{
				selectedPrimitiveEditorProxy.cancelEdit(selectedPrimitive);
				selectedPrimitiveEditorProxy = null;
			}
			// Suppress any cursor updates.
			selectedPrimitive = null;
		}
	}

	/**
	 * End and commits the active editor.
	 *
	 * @see EditorProxy#endEdit(DrawPrimitive, Graphics2D)
	 */
	protected void endEdit()
	{
		if (selectedPrimitiveEditor != null)
		{
			if (selectedPrimitiveEditorProxy != null)
			{
				Graphics2D g2 = (Graphics2D) getGraphics();
				g2.translate(offsetX, offsetY);
				g2.scale(configuration.scale, configuration.scale);
				selectedPrimitiveEditorProxy.endEdit(selectedPrimitive, g2);
			}
			selectedPrimitive = null;
			removePrimitiveEditor();
		}
	}

	/**
	 * Removes the primitive in-place editor.
	 */
	protected void removePrimitiveEditor()
	{
		if (selectedPrimitiveEditor != null)
		{
			selectedPrimitiveEditor.removeKeyListener(editorKeyAdapter);
			selectedPrimitiveEditor.removeFocusListener(editorFocusAdapter);
			remove(selectedPrimitiveEditor);
			selectedPrimitiveEditor = null;
		}
	}
}

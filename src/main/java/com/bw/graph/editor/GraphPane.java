package com.bw.graph.editor;

import com.bw.graph.DrawStyle;
import com.bw.graph.GraphConfiguration;
import com.bw.graph.VisualModel;
import com.bw.graph.editor.action.EditAction;
import com.bw.graph.primitive.DrawPrimitive;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.visual.Visual;
import com.bw.graph.visual.VisualFlags;
import com.bw.svg.SVGAttribute;
import com.bw.svg.SVGWriter;

import javax.swing.JComponent;
import javax.swing.JViewport;
import javax.swing.SwingUtilities;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Paint;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Rectangle2D;
import java.io.StringWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
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
	private GraphConfiguration _configuration;

	/**
	 * Stack of current editor-actions.
	 */
	private Deque<EditAction> _actionStack = new ArrayDeque<>(10);


	/**
	 * Listeners.
	 */
	private final LinkedList<InteractionListener> _listeners = new LinkedList<>();

	/**
	 * Queue of parent states we had entered.
	 */
	protected LinkedList<Visual> _parents = new LinkedList<>();

	/**
	 * Milliseconds needed of last paint cycle.
	 */
	private long _lastPaintMS;

	/**
	 * If true {@link #_lastPaintMS} is shown on screen for debugging.
	 */
	private boolean _showDrawSpeed = true;

	/**
	 * Listen to key events.
	 */
	protected KeyAdapter _keyAdapter = new KeyAdapter()
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
	protected FocusListener _editorFocusAdapter = new FocusAdapter()
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
	protected KeyAdapter _editorKeyAdapter = new KeyAdapter()
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
	private Visual _draggingVisual;

	/**
	 * The visual that is currently clicked.
	 */
	private Visual _clickedVisual;


	/**
	 * The last coordinate of a drag-event.
	 */
	private final Point _lastDragPoint = new Point(0, 0);

	/**
	 * Current visual the mouse is over.
	 */
	private Visual _mouseOverVisual;

	/**
	 * Listens to clicks and drags on visuals.
	 */
	protected MouseListener _mouseHandler = new MouseListener()
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
				x -= _offsetX;
				y -= _offsetY;

				x /= _configuration._scale;
				y /= _configuration._scale;

				DrawPrimitive editablePrimitive = clicked.getEditablePrimitiveAt(x, y);
				if (editablePrimitive == null)
				{
					ModelPrimitive modelPrimitive = clicked.getPrimitiveOf(ModelPrimitive.class);
					if (modelPrimitive != null)
					{
						Rectangle2D.Float subModelBox = clicked.getAbsoluteBoundsOfPrimitive(null, modelPrimitive);
						if (subModelBox != null && subModelBox.contains(x, y))
						{
							_parents.add(clicked);
							setModel(modelPrimitive.getChildModel());
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
			_clickedVisual = getVisualAt(_lastDragPoint.x = e.getX(), _lastDragPoint.y = e.getY());
			if (_clickedVisual != null)
				System.err.println("Pressed on " + _clickedVisual.getClass()
																 .getName() + " " + _clickedVisual);
			setSelectedVisual(_clickedVisual);
			SwingUtilities.convertPointToScreen(_lastDragPoint, GraphPane.this);
		}

		@Override
		public void mouseReleased(MouseEvent e)
		{
			boolean fireDragged = _draggingVisual != null;
			if (fireDragged)
			{
				EditAction action = _draggingVisual.endDrag();
				if (action != null)
					_actionStack.push(action);
				_draggingVisual = null;
			}
			_clickedVisual = null;
			_lastDragPoint.x = _lastDragPoint.y = 0;
			if (fireDragged)
				fireMouseDragging(null);
		}

	};

	/**
	 * Handles mouse wheel.
	 */
	protected MouseWheelListener _mouseWheelHandler = new MouseWheelListener()
	{
		@Override
		public void mouseWheelMoved(MouseWheelEvent we)
		{
			if (_configuration._zoomByMetaMouseWheelEnabled)
			{
				int wheel = we.getWheelRotation();
				if (wheel != 0)
				{
					int mod = we.getModifiersEx();
					if ((mod & (InputEvent.CTRL_DOWN_MASK | InputEvent.META_DOWN_MASK)) != 0)
					{
						float scale = _configuration._scale - 0.1f * wheel;
						if (scale >= 0.1)
						{
							_configuration._scale = scale;
							SwingUtilities.invokeLater(() ->
							{
								if (_configuration._buffered)
									_model.repaint();
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
	protected MouseMotionListener _mouseMotionHandler = new MouseMotionListener()
	{
		@Override
		public void mouseMoved(MouseEvent e)
		{
			Visual over = getVisualAt(e.getX(), e.getY());
			if (_mouseOverVisual != over)
			{
				Rectangle2D.Float update = null;
				if (_mouseOverVisual != null)
				{
					update = _mouseOverVisual.getAbsoluteBounds2D(null);
				}

				_mouseOverVisual = over;
				fireMouseOver(_mouseOverVisual);

				if (_mouseOverVisual != null)
				{
					Rectangle2D.Float rt = _mouseOverVisual.getAbsoluteBounds2D(null);
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

			final float scale = _configuration._scale;

			if (_draggingVisual == null && _clickedVisual != null)
			{
				_draggingVisual = _clickedVisual;
				_draggingVisual.startDrag((mp.x - _offsetX) / scale, (mp.y - _offsetY) / scale);
				fireMouseDragging(_draggingVisual);
			}

			SwingUtilities.convertPointToScreen(mp, GraphPane.this);

			final int xd = mp.x - _lastDragPoint.x;
			final int yd = mp.y - _lastDragPoint.y;

			if (_draggingVisual != null)
			{
				_draggingVisual.dragBy(xd / scale, yd / scale);
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
			_lastDragPoint.setLocation(mp);
		}
	};

	/**
	 * Creates a new graph pane.
	 *
	 * @param configuration The configuration.
	 */
	public GraphPane(GraphConfiguration configuration)
	{
		_configuration = configuration;
		setLayout(null);
		setModel(new VisualModel("none"));
		addMouseListener(_mouseHandler);
		addMouseMotionListener(_mouseMotionHandler);
		addMouseWheelListener(_mouseWheelHandler);
		addKeyListener(_keyAdapter);
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
		x -= _offsetX;
		y -= _offsetY;

		x /= _configuration._scale;
		y /= _configuration._scale;

		var visuals = _model.getVisuals();
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
	protected VisualModel _model;

	/**
	 * Drawing X-offset.
	 */
	protected float _offsetX = 0;

	/**
	 * Drawing Y-offset.
	 */
	protected float _offsetY = 0;

	/**
	 * Last selected visual or null.
	 */
	protected Visual _selectedVisual;

	/**
	 * The current selected primitive.
	 */
	protected DrawPrimitive _selectedPrimitive;

	/**
	 * The current active editor component.
	 */
	protected JComponent _selectedEditorComponent;

	/**
	 * True if the current active editor is shown in-place.
	 */
	protected boolean _selectedEditorIsInPlace = false;


	/**
	 * The current active editor dialog (if not in-place).
	 * Is lazy created and will be reused.
	 */
	protected DialogHandler _dialogHandler;


	/**
	 * The proxy of the current active editor.
	 */
	protected Editor _selectedEditor;


	@Override
	protected void paintComponent(Graphics g)
	{
		final long start = System.currentTimeMillis();
		Graphics2D g2 = (Graphics2D) g.create();
		g2.translate(_offsetX, _offsetY);

		if (_configuration._antialiasing)
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		try
		{
			if (isOpaque())
			{
				g2.setPaint(_configuration._graphBackground == null ? getBackground() : _configuration._graphBackground);
				g2.fillRect(0, 0, getWidth(), getHeight());
			}
			g2.scale(_configuration._scale, _configuration._scale);

			_model.draw(g2);
			if (_selectedPrimitive != null && _selectedEditorComponent == null)
				drawPrimitiveCursor(g2, _selectedPrimitive);

		}
		finally
		{
			g2.dispose();

			final long end = System.currentTimeMillis();
			_lastPaintMS = end - start;

			if (_showDrawSpeed)
			{
				g.setColor(getForeground());
				g.setFont(getFont());
				char[] text = (Long.toString(_lastPaintMS) + "ms").toCharArray();
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
		sw._precisionFactor = _configuration._precisionFactor;

		Rectangle2D.Float bounds = getBounds2D();
		sw.startSVG(bounds);

		Paint bg = _configuration._graphBackground == null ? getBackground() : _configuration._graphBackground;
		if (bg != null)
		{
			sw.startStyle();
			sw.writeAttribute(SVGAttribute.BackgroundColor, bg);
			sw.endStyle();
		}
		if (_model._name != null)
		{
			sw.startElement("title");
			sw.startContent();
			sw.writeEscaped(_model._name);
			sw.endElement();
		}
		for (Visual v : _model.getVisuals())
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
		if (model != this._model)
		{
			cancelEdit();

			boolean fireHierarchy = false;
			while (!_parents.isEmpty())
			{
				if (ModelPrimitive.getChildModel(_parents.peekLast()) == model)
					break;
				_parents.removeLast();
				fireHierarchy = true;
			}

			Visual oldSelected = _selectedVisual;
			_selectedVisual = null;
			_selectedPrimitive = null;
			if (this._model != null)
			{
				this._model.removeListener(this::repaint);
			}
			if (model == null)
				model = new VisualModel("none");
			this._model = model;
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
		return _model;
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

		Visual oldSelected = _selectedVisual;
		_selectedVisual = visual;

		if (oldSelected != null && oldSelected != _selectedVisual)
		{
			if (oldSelected.isFlagSet(VisualFlags.SELECTED))
			{
				oldSelected.clearFlags(VisualFlags.SELECTED);
				triggerRepaint = true;
			}
		}

		if (_selectedVisual != null && !_selectedVisual.isFlagSet(VisualFlags.SELECTED))
		{
			visual.setFlags(VisualFlags.SELECTED);
			triggerRepaint = true;
		}
		List<Visual> visuals = _model.getVisuals();
		if (_selectedVisual != null && visuals.indexOf(_selectedVisual) != (visuals.size() - 1))
		{
			if (_model.moveVisualToTop(_selectedVisual))
			{
				// Repaint will be triggered my model listener
				triggerRepaint = false;
			}
		}

		if (oldSelected != null && oldSelected != _selectedVisual)
		{
			if (_selectedVisual == null)
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
	 * Get the selected visual.
	 *
	 * @return the current selected visual.
	 */
	public Visual getSelectedVisual()
	{
		return _selectedVisual;
	}


	/**
	 * Sets the current selected primitive.
	 *
	 * @param p The primitive
	 */
	public void setSelectedPrimitive(DrawPrimitive p)
	{
		if (p != _selectedPrimitive)
		{
			cancelEdit();
			Graphics2D g2 = (Graphics2D) getGraphics();
			try
			{
				g2.translate(_offsetX, _offsetY);
				g2.scale(_configuration._scale, _configuration._scale);

				if (_selectedPrimitive != null && _selectedEditorComponent == null)
				{
					drawPrimitiveCursor(g2, _selectedPrimitive);
				}
				_selectedPrimitive = p;
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

		Rectangle2D.Float rt = v.getAbsoluteBoundsOfPrimitive(g2, primitive);
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
		_selectedVisual = null;
		_model = null;
		removeMouseListener(_mouseHandler);
		removeMouseMotionListener(_mouseMotionHandler);
		removeMouseWheelListener(_mouseWheelHandler);
		_listeners.clear();
		_parents.clear();
		_mouseHandler = null;
		_mouseMotionHandler = null;
		_mouseWheelHandler = null;
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
		Rectangle2D.Float bounds = _model.getBounds2D((Graphics2D) getGraphics());
		bounds.x *= _configuration._scale;
		bounds.y *= _configuration._scale;
		bounds.height = 5 + bounds.height * _configuration._scale;
		bounds.width = 5 + bounds.width * _configuration._scale;

		return bounds;
	}

	/**
	 * Gets the graph configuration.
	 *
	 * @return The getGraphConfiguration, never null.
	 */
	public GraphConfiguration getGraphConfiguration()
	{
		return _configuration;
	}

	/**
	 * Adds a new interaction listener.
	 *
	 * @param listener The listener to add.
	 */
	public void addInteractionListener(InteractionListener listener)
	{
		this._listeners.remove(listener);
		this._listeners.add(listener);
	}

	/**
	 * Removes an interaction listener.
	 *
	 * @param listener The listener to add.
	 */
	public void removeInteractionListener(InteractionListener listener)
	{
		this._listeners.remove(listener);
	}

	/**
	 * Calls {@link InteractionListener#selected(Visual)} on all listeners.
	 */
	protected void fireVisualSelected()
	{
		new ArrayList<>(_listeners).forEach(listener -> listener.selected(_selectedVisual));
	}

	/**
	 * Calls {@link InteractionListener#deselected(Visual)} on all listeners.
	 *
	 * @param oldSelected The de-selected visual.
	 */
	protected void fireVisualDeselected(Visual oldSelected)
	{
		new ArrayList<>(_listeners).forEach(listener -> listener.deselected(oldSelected));
	}

	/**
	 * Calls hierarchyChanged on all listeners.
	 */
	protected void fireHierarchyChanged()
	{
		new ArrayList<>(_listeners).forEach(InteractionListener::hierarchyChanged);
	}

	/**
	 * Calls mouseDragging on all listeners.
	 *
	 * @param visual The visual that is dragged or null.
	 */
	protected void fireMouseDragging(Visual visual)
	{
		new ArrayList<>(_listeners).forEach(i -> i.mouseDragging(visual));
	}

	/**
	 * Calls mouseOver on all listeners.
	 *
	 * @param visual The visual the mouse is over or null.
	 */
	protected void fireMouseOver(Visual visual)
	{
		new ArrayList<>(_listeners).forEach(i -> i.mouseOver(visual));
	}

	/**
	 * Get current hierarchy
	 *
	 * @return The chain of parents the editor entered.
	 */
	public List<Visual> getHierarchy()
	{
		return Collections.unmodifiableList(_parents);
	}


	/**
	 * Start edit of a primitive. There can be only one active edit.
	 *
	 * @param g2 The graphics context to use.
	 */
	protected void startEdit(Graphics2D g2)
	{
		cancelEdit();
		Editor editor = _selectedPrimitive == null ? null : _selectedPrimitive.getEditor();
		if (editor == null)
		{
			editor = _selectedVisual == null ? null : _selectedVisual.getEditor();
		}

		if (editor != null)
		{
			_selectedEditor = editor;
			Visual v = _selectedPrimitive.getVisual();
			if (_selectedEditorComponent != null)
			{
				// This should not happen...
				System.err.println("Previous Editor still active during new selection, should already by removed.");
				removeEditor();
			}
			_selectedEditorComponent = _selectedEditor.getEditor(_selectedPrimitive);
			_selectedEditorIsInPlace = _selectedEditor.isInPlace();

			if (_selectedEditorIsInPlace)
			{
				Rectangle2D.Float rt = v.getAbsoluteBoundsOfPrimitive(g2, _selectedPrimitive);

				final float scale = _configuration._scale;
				rt.x += _offsetX;
				rt.y += _offsetY;
				rt.x *= scale;
				rt.y *= scale;
				rt.width *= scale;
				rt.height *= scale;

				Font font;
				DrawStyle style = _selectedPrimitive.getStyle();
				FontMetrics fontMetrics = style.getFontMetrics();
				if (fontMetrics != null)
				{
					font = style.getFont();
				}
				else
				{
					font = getFont();
					fontMetrics = getFontMetrics(font);
				}
				font = font.deriveFont((float) (int) (0.5 + font.getSize() * _configuration._scale));
				_selectedEditorComponent.setFont(font);
				Dimension d = _selectedEditorComponent.getPreferredSize();

				float minWidth = fontMetrics.charWidth('X') * 20 * _configuration._scale;
				d.width = (int) (0.5 + Math.max(minWidth, d.width));

				rt.x += (rt.width - d.width) / 2f;
				rt.y += (rt.height - d.height) / 2f;
				rt.width = d.width;
				rt.height = d.height;

				_selectedEditorComponent.setBounds(rt.getBounds());
				_selectedEditorComponent.addKeyListener(_editorKeyAdapter);
				_selectedEditorComponent.addFocusListener(_editorFocusAdapter);
				add(_selectedEditorComponent);
				_selectedEditorComponent.requestFocus();
			}
			else
			{
				if (_dialogHandler == null)
				{
					_dialogHandler = new DefaultDialogHandler();
				}

				_dialogHandler.openEditor(this, _selectedEditorComponent, this::endEdit, this::cancelEdit);
			}

		}
		if (_selectedPrimitive != null)
		{
			drawPrimitiveCursor(g2, _selectedPrimitive);
		}
	}

	/**
	 * Cancel any active editor.
	 *
	 * @see Editor#cancelEdit(DrawPrimitive)
	 */
	protected void cancelEdit()
	{
		if (_selectedEditorComponent != null)
		{
			removeEditor();
			if (_selectedEditor != null)
			{
				_selectedEditor.cancelEdit(_selectedPrimitive);
				_selectedEditor = null;
			}
			// Suppress any cursor updates.
			_selectedPrimitive = null;
		}
	}

	/**
	 * End and commits the active editor.
	 *
	 * @see Editor#endEdit(DrawPrimitive, VisualModel, Graphics2D)
	 */
	protected void endEdit()
	{
		if (_selectedEditorComponent != null)
		{
			if (_selectedEditor != null)
			{
				Graphics2D g2 = (Graphics2D) getGraphics();
				g2.translate(_offsetX, _offsetY);
				g2.scale(_configuration._scale, _configuration._scale);
				EditAction action = _selectedEditor.endEdit(_selectedPrimitive, _model, g2);
				if (action != null)
					_actionStack.push(action);
			}
			_selectedPrimitive = null;
			removeEditor();
		}
	}

	/**
	 * Removes the current editor.
	 */
	protected void removeEditor()
	{
		if (_selectedEditorComponent != null)
		{
			_selectedEditorComponent.removeKeyListener(_editorKeyAdapter);
			_selectedEditorComponent.removeFocusListener(_editorFocusAdapter);

			if (_selectedEditorIsInPlace)
			{
				remove(_selectedEditorComponent);
			}
			else
			{
				if (_dialogHandler != null)
				{
					_dialogHandler.closeEditor();
					_dialogHandler = null;
				}
			}
			_selectedEditorComponent = null;
		}
	}

	/**
	 * Sets the dialog handler.
	 *
	 * @param dialogHandler The dialog handler, if null, the default handler is restored.
	 */
	public void setDialogHandler(DialogHandler dialogHandler)
	{
		if (_dialogHandler != null && _dialogHandler != dialogHandler)
		{
			cancelEdit();
		}
		_dialogHandler = dialogHandler;
	}

	/**
	 * Get the outstanding editor actions.
	 *
	 * @return The list of actions, possibly empty but never null.
	 * @see #commitActions()
	 */
	public Deque<EditAction> getEditActions()
	{
		return _actionStack;
	}

	/**
	 * Commits the current edit actions.<br>
	 * Clear action stack and resets the modified flags.
	 */
	public void commitActions()
	{
		_actionStack.clear();
		getModel().clearFlags(VisualFlags.MODIFIED);
	}
}

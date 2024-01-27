package com.bw.modelthings.intellij.editor;

import com.bw.graph.VisualModel;
import com.bw.graph.editor.InteractionAdapter;
import com.bw.graph.primitive.ModelPrimitive;
import com.bw.graph.primitive.Text;
import com.bw.graph.visual.GenericPrimitiveVisual;
import com.bw.graph.visual.Visual;
import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.parser.LogExtensionParser;
import com.bw.modelthings.fsm.parser.ParserException;
import com.bw.modelthings.fsm.parser.ScxmlTags;
import com.bw.modelthings.fsm.parser.XmlParser;
import com.bw.modelthings.fsm.ui.GraphExtension;
import com.bw.modelthings.fsm.ui.GraphFactory;
import com.bw.modelthings.fsm.ui.StateNameProxy;
import com.intellij.ide.ui.customization.CustomActionsSchema;
import com.intellij.openapi.actionSystem.ActionGroup;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.ActionPlaces;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.WriteCommandAction;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.fileEditor.FileEditorState;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlDocument;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.Timer;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Graphical SCXML {@link FileEditor}.
 */
public class ScxmlGraphEditor extends UserDataHolderBase implements FileEditor
{
	private static final Logger LOG = Logger.getInstance(ScxmlGraphEditor.class);

	/**
	 * The Graphical Editor component.
	 */
	final ScxmlGraphPanel _component;

	/**
	 * The file that is shown.
	 */
	final VirtualFile _file;

	/**
	 * The XML file that is shown (Psi file for {@link #_file}).
	 */
	PsiFile _xmlFile;

	/**
	 * The document of the xml file.
	 */
	Document _xmlDocument;

	/**
	 * True if an update of the graph was triggered.
	 */
	boolean _updateGraphTriggered = false;

	/**
	 * True if an update of the XML file was triggered.
	 */
	boolean _updateXmlTriggered = false;

	/**
	 * Sync to document is in progress, if this flag is true.
	 */
	boolean _inDocumentSync = false;

	/**
	 * Enables file/graph synchronization.
	 */
	boolean _enableSync = true;

	/**
	 * Timer to trigger graph to document synchronization.
	 */
	Timer _updateXmlTimer;


	/**
	 * Listener for document changes.
	 */
	com.intellij.openapi.editor.event.DocumentListener _documentListener = new DocumentListener()
	{
		@Override
		public void documentChanged(@NotNull DocumentEvent event)
		{
			if (_inDocumentSync)
			{
				LOG.warn("(Sync) Ignored Document Event " + event);
			}
			else
			{
				LOG.warn("Document Event " + event);
				triggerGraphUpdate();
			}
		}
	};

	/**
	 * Triggers a background update of the graph.
	 */
	protected void triggerGraphUpdate()
	{
		if (!_updateGraphTriggered)
		{
			_updateGraphTriggered = true;
			// Executes in worker thread with read-lock.
			ApplicationManager.getApplication()
							  .executeOnPooledThread(() -> ApplicationManager.getApplication()
																			 .runReadAction(this::runGraphUpdate));
		}
	}

	/**
	 * Triggers a background update of the XML file.
	 */
	protected void triggerXmlUpdate()
	{
		if (!_updateXmlTriggered)
		{
			_updateXmlTriggered = true;
			ApplicationManager.getApplication()
							  .invokeLater(() -> WriteCommandAction.runWriteCommandAction(_xmlFile.getProject(), this::runXmlUpdate));
		}
	}

	private Visual getStartVisual(VisualModel model)
	{
		return model.getVisuals()
					.stream()
					.filter(v -> v.isFlagSet(GraphFactory.START_NODE_FLAG))
					.findFirst()
					.orElse(null);
	}

	/**
	 * This method is synchronized because it is possible that this method is called from different worker threads in parallel.
	 */
	private synchronized void runXmlUpdate()
	{
		// @TODO: move calculation of graphextensions to worker thread, then use the result here in write action.
		if (_updateXmlTriggered)
		{
			_updateXmlTriggered = false;
			try
			{
				_inDocumentSync = true;

				VisualModel model = (_component._root == null) ? null : ModelPrimitive.getChildModel(_component._root);
				if (_xmlFile != null && model != null)
				{
					//////////////////////////////////////////
					// Collect data from model
					HashMap<String, GraphExtension.PosAndBounds> bounds = new HashMap<>();
					HashMap<String, GraphExtension.PosAndBounds> startBounds = new HashMap<>();
					HashMap<String, String> statesRenamed = new HashMap<>();

					List<Visual> visuals = new ArrayList<>(model.getVisuals());
					Visual sv = getStartVisual(model);
					if (sv != null)
						startBounds.put(model._name, new GraphExtension.PosAndBounds(sv.getAbsolutePosition(), sv.getAbsoluteBounds2D(null)));

					while (!visuals.isEmpty())
					{
						Visual v = visuals.remove(visuals.size() - 1);
						if (v instanceof GenericPrimitiveVisual)
						{
							Text text = v.getPrimitiveOf(Text.class);
							Object userData = text == null ? null : text.getUserData();
							if (userData instanceof StateNameProxy proxy)
							{
								String id = (String) v.getId();
								if (id != null)
								{
									bounds.put(id, new GraphExtension.PosAndBounds(v.getAbsolutePosition(), v.getAbsoluteBounds2D(null)));
									if (!id.equals(proxy._nameInFile))
									{
										// State was renamed
										statesRenamed.put(proxy._nameInFile, id);
										System.err.println("Renamed: " + proxy._nameInFile + " -> " + id);
										proxy._nameInFile = id;
									}
								}
								VisualModel m = ModelPrimitive.getChildModel(v);
								if (m != null)
								{
									if (m._name != null)
									{
										sv = getStartVisual(m);
										if (sv != null)
										{
											startBounds.put(m._name, new GraphExtension.PosAndBounds(sv.getAbsolutePosition(), sv.getAbsoluteBounds2D(null)));
											System.err.println(m._name + " = " + startBounds.get(m._name));
										}
									}
									else
									{
										System.err.println("Submodel of " + v.getId() + " without name");
									}
									visuals.addAll(m.getVisuals());
								}
							}
						}
					}

					//////////////////////////////////////////
					// Update psi file
					try
					{
						// @TODO Try to avoid to write to disk every time we update!
						PsiDocumentManager.getInstance(_xmlFile.getProject())
										  .commitDocument(_xmlDocument);

						final float precisionFactor = this._component.getGraphConfiguration()._precisionFactor;
						XmlDocument doc = ((XmlFile) _xmlFile).getDocument();
						XmlTag root = doc.getRootTag();
						if (root != null)
						{
							List<XmlTag> allStates = new ArrayList<>(100);
							allStates.add(root);

							while (!allStates.isEmpty())
							{
								XmlTag s = allStates.remove(allStates.size() - 1);

								if (!statesRenamed.isEmpty())
								{
									// Renaming
									renameInNameList(s, ScxmlTags.ATTR_ID, null, statesRenamed);
									renameInNameList(s, ScxmlTags.ATTR_INITIAL, null, statesRenamed);
									renameInNameList(s.findSubTags(ScxmlTags.TAG_TRANSITION, ScxmlTags.NS_SCXML), ScxmlTags.ATTR_TARGET, null, statesRenamed);

									XmlTag initial = s.findFirstSubTag(ScxmlTags.TAG_INITIAL);
									if (initial != null)
										renameInNameList(initial.findSubTags(ScxmlTags.TAG_TRANSITION, ScxmlTags.NS_SCXML), ScxmlTags.ATTR_TARGET, null, statesRenamed);
								}

								String id = s.getAttributeValue(ScxmlTags.ATTR_ID);

								List<XmlTag> states = Arrays.asList(s.findSubTags(ScxmlTags.TAG_STATE, ScxmlTags.NS_SCXML));
								List<XmlTag> parallels = Arrays.asList(s.findSubTags(ScxmlTags.TAG_PARALLEL, ScxmlTags.NS_SCXML));

								allStates.addAll(states);
								allStates.addAll(parallels);

								if (id != null)
								{
									GraphExtension.PosAndBounds r = bounds.get(id);
									if (r != null)
									{
										String bs = r.toXML(precisionFactor);
										XmlAttribute attr = s.getAttribute(GraphExtension.ATTR_BOUNDS, GraphExtension.NS_GRAPH_EXTENSION);
										if (attr == null)
										{
											s.setAttribute(GraphExtension.ATTR_BOUNDS, GraphExtension.NS_GRAPH_EXTENSION, bs);
										}
										else
										{
											if (!bs.equals(attr.getValue()))
											{
												attr.setValue(bs);
											}
										}
									}
								}

								GraphExtension.PosAndBounds startNodeBounds = startBounds.get(id);

								if (startNodeBounds != null || !(states.isEmpty() && parallels.isEmpty()))
								{

									// A submachine. We need to set the start-state bounds
									if (startNodeBounds == null)
									{
										startNodeBounds = startBounds.get(s.getAttributeValue(ScxmlTags.ATTR_NAME, ScxmlTags.NS_SCXML));
									}
									if (startNodeBounds != null)
									{
										String bs = startNodeBounds.toXML(precisionFactor);
										XmlAttribute attrStart = s.getAttribute(GraphExtension.ATTR_START_BOUNDS, GraphExtension.NS_GRAPH_EXTENSION);
										if (attrStart == null)
										{
											s.setAttribute(GraphExtension.ATTR_START_BOUNDS, GraphExtension.NS_GRAPH_EXTENSION, bs);
										}
										else
										{
											if (!bs.equals(attrStart.getValue()))
											{
												attrStart.setValue(bs);
											}
										}
									}
								}
							}
						}
						PsiDocumentManager.getInstance(_xmlFile.getProject())
										  .commitDocument(_xmlDocument);
					}
					catch (ProcessCanceledException pce)
					{
						// Shall never be caught
						throw pce;
					}
				}
			}
			finally
			{
				_inDocumentSync = false;
				LOG.warn("XML updated");
				// @TODO some unlock?
			}
		}
	}

	/**
	 * Temporary used buffer. Usage is not thread safe, shall be used only from synchronized methods.
	 */
	private final StringBuilder _tempName = new StringBuilder();

	/**
	 * Method is not thread-safe, must be called only from synchronized methods.
	 *
	 * @param tags         The Tags to process.
	 * @param attribute    The attribute to change.
	 * @param namespace    The namespace of the attribute.
	 * @param replacements The replacements.
	 */
	private void renameInNameList(XmlTag[] tags, String attribute, String namespace, Map<String, String> replacements)
	{
		for (XmlTag tag : tags)
		{
			renameInNameList(tag, attribute, namespace, replacements);
		}
	}

	/**
	 * Method is not thread-safe, must be called only from synchronized methods.
	 *
	 * @param tag          The Tag to process.
	 * @param attribute    The attribute to change.
	 * @param namespace    The namespace of the attribute.
	 * @param replacements The replacements.
	 */
	private void renameInNameList(XmlTag tag, String attribute, String namespace, Map<String, String> replacements)
	{
		XmlAttribute attr = tag.getAttribute(attribute, namespace);
		if (attr != null)
		{
			_tempName.setLength(0);
			boolean changed = false;
			for (String name : ScxmlTags.splitNameList(attr.getValue()))
			{
				if (!_tempName.isEmpty())
					_tempName.append(' ');
				String newName = replacements.get(name);
				if (newName == null)
				{
					_tempName.append(name);
				}
				else
				{
					changed = true;
					_tempName.append(newName);
				}
			}
			if (changed)
				attr.setValue(_tempName.toString());
		}
	}

	/**
	 * This method is synchronized because it is possible that this method is called from different worker threads in parallel.
	 */
	private synchronized void runGraphUpdate()
	{
		if (_updateGraphTriggered)
		{
			_updateGraphTriggered = false;
			if (_file != null)
			{
				try
				{
					XmlParser parser = new XmlParser();
					parser.addExtensionParser("*", new LogExtensionParser());

					GraphExtension ge = new GraphExtension();
					parser.addExtensionParser(GraphExtension.NS_GRAPH_EXTENSION, ge);

					final FiniteStateMachine fsm = parser.parse(_file.toNioPath(), _xmlDocument.getText());
					ApplicationManager.getApplication()
									  .invokeLater(() -> _component.setStateMachine(fsm, ge));
				}
				catch (ProcessCanceledException pce)
				{
					// Shall never be caught
					throw pce;
				}
				catch (ParserException pe)
				{
					_component.setError(pe);
				}
			}
		}
	}

	/**
	 * Creates a new editor.
	 *
	 * @param file    The original file
	 * @param psiFile The matching psi file.
	 */
	public ScxmlGraphEditor(@NotNull VirtualFile file, @Nullable PsiFile psiFile)
	{
		_component = new ScxmlGraphPanel(psiFile == null ? null : psiFile.getProject());
		this._file = file;
		setXmlFile(psiFile);

		_updateXmlTimer = new Timer(2000, e ->
		{
			if (_component._root != null)
			{
				VisualModel model = ModelPrimitive.getChildModel(_component._root);
				if (model != null && model.isModified())
				{
					if (_enableSync)
					{
						LOG.warn("Model modified, sync with file");
						model.setModified(false);
						triggerXmlUpdate();
					}
					else
					{
						LOG.warn("Model modified, but sync disabled");
					}
				}
			}
		});
		_updateXmlTimer.start();

		_component._pane.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent event)
			{
				if (event.isPopupTrigger() && !event.isConsumed())
				{
					ActionGroup ag = (ActionGroup) CustomActionsSchema.getInstance()
																	  .getCorrectedAction("ScXMLPopupMenu");
					JPopupMenu popupMenu = ActionManager.getInstance()
														.createActionPopupMenu(ActionPlaces.EDITOR_POPUP, ag)
														.getComponent();
					popupMenu.show(_component, event.getX(), event.getY());
				}

			}
		});

		_component._pane.addInteractionListener(new InteractionAdapter()
		{
			@Override
			public void deselected(Visual visual)
			{
				_enableSync = true;
			}

			@Override
			public void mouseDragging(Visual visual)
			{
				// Disable sync to xml file during dragging.
				_enableSync = (visual == null);
			}

			@Override
			public void mouseOver(Visual visual)
			{
				LOG.warn("MouseOver " + visual);
			}
		});


	}

	/**
	 * Sets the XML Psi file.
	 *
	 * @param xmlFile The file, can be null.
	 */
	public void setXmlFile(PsiFile xmlFile)
	{
		if (this._xmlDocument != null)
		{
			this._xmlDocument.removeDocumentListener(_documentListener);
		}
		this._xmlFile = xmlFile;
		if (xmlFile == null)
		{
			_component.setError(null);
		}
		else
		{
			_xmlDocument = PsiDocumentManager.getInstance(xmlFile.getProject())
											 .getDocument(xmlFile);
			if (_xmlDocument != null)
			{
				LOG.warn("Document " + _xmlDocument + " " + _xmlDocument.getClass()
																		.getName());
				_xmlDocument.addDocumentListener(_documentListener);
			}
			triggerGraphUpdate();
		}
	}

	@Override
	public @NotNull JComponent getComponent()
	{
		return _component;
	}

	@Override
	public @Nullable JComponent getPreferredFocusedComponent()
	{
		return _component;
	}

	@Override
	public @Nls(capitalization = Nls.Capitalization.Title) @NotNull String getName()
	{
		return "Scxml Graph";
	}

	@Override
	public void setState(@NotNull FileEditorState state)
	{
	}

	@Override
	public boolean isModified()
	{
		return false;
	}

	@Override
	public boolean isValid()
	{
		return true;
	}

	@Override
	public void addPropertyChangeListener(@NotNull PropertyChangeListener listener)
	{
		_component.addPropertyChangeListener(listener);
	}

	@Override
	public void removePropertyChangeListener(@NotNull PropertyChangeListener listener)
	{
		_component.removePropertyChangeListener(listener);
	}

	@Override
	public VirtualFile getFile()
	{
		return _file;
	}

	@Override
	public void dispose()
	{
		if (_component != null)
			_component.dispose();
		if (_xmlDocument != null)
		{
			_xmlDocument.removeDocumentListener(_documentListener);
			_xmlDocument = null;
		}
	}
}

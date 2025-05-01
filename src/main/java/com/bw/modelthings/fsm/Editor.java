package com.bw.modelthings.fsm;

import com.bw.modelthings.fsm.model.FiniteStateMachine;
import com.bw.modelthings.fsm.model.State;
import com.bw.modelthings.fsm.parser.LogExtensionParser;
import com.bw.modelthings.fsm.parser.XmlParser;
import com.bw.modelthings.fsm.ui.FsmGraphPanel;
import com.bw.modelthings.fsm.ui.ScxmlGraphExtension;
import com.bw.modelthings.fsm.ui.StateVisual;


import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.geom.Point2D;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 * Standalone version (just for testing)
 */
public class Editor extends JFrame
{
	private static JFileChooser scxmlFileChooser;
	private FsmGraphPanel graph_;
	private Path lastScxmlFile;

	JCheckBoxMenuItem antialiasingMenuCheck = new JCheckBoxMenuItem("Antialiasing");
	JCheckBoxMenuItem fractionalMetricsMenuCheck = new JCheckBoxMenuItem("Fractional Metrics");

	public static final String PREFS_LAST_FILE = "lastFile";
	public static final String PREFS_ANTIALIASING = "graph.Antialiasing";
	public static final String PREFS_FRACTIONAL_METRICS = "graph.FractionalMetrics";

	JPopupMenu popupMenu = new JPopupMenu();

	public Action deleteStateAction;
	public Action newStateAction;

	public Point2D.Float lastMousePos;

	public Editor(String title)
	{
		super(title);
		setLayout(new BorderLayout());

		graph_ = new FsmGraphPanel();
		graph_.setPreferredSize(new Dimension(600, 400));

		deleteStateAction = new AbstractAction("Delete State")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				var pane = graph_.getGraphPane();
				if (pane.getSelectedVisual() instanceof StateVisual stateVisual)
				{
					State s = stateVisual.getState();
					if (s != null)
						graph_.removeState(s, true);
				}
			}
		};

		newStateAction = new AbstractAction("New State")
		{
			@Override
			public void actionPerformed(ActionEvent e)
			{
				String name = JOptionPane.showInputDialog("New State Name");
				if (name != null && !name.isEmpty())
				{
					graph_.addState(null, name, lastMousePos);
				}
			}
		};

		popupMenu.add(deleteStateAction);
		popupMenu.add(newStateAction);


		graph_.getGraphPane()
			  .addMouseListener(new MouseAdapter()
			  {
				  @Override
				  public void mouseReleased(MouseEvent event)
				  {
					  if (event.isPopupTrigger() && !event.isConsumed())
					  {
						  lastMousePos = graph_.getGraphPane()
											   .toModelCoordinates(event.getX(), event.getY());
						  var pane = graph_.getGraphPane();
						  deleteStateAction.setEnabled(pane.getSelectedVisual() instanceof StateVisual);
						  popupMenu.show(graph_, event.getX(), event.getY());
					  }

				  }
			  });

		add(graph_, BorderLayout.CENTER);

		graph_.setStyle(Collections.emptyMap());

		JMenuBar menu = new JMenuBar();

		JMenuItem loadMenuItem = new JMenuItem("Open...", KeyEvent.VK_L);
		loadMenuItem.setToolTipText("<html>Opens a SCXML file.</html>");
		loadMenuItem.addActionListener(e ->
		{
			JFileChooser fs = getSCXMLFileChooser();
			int returnVal = fs.showOpenDialog(this);
			if (returnVal == JFileChooser.APPROVE_OPTION)
			{
				openFsm(fs.getSelectedFile()
						  .toPath());
			}
		});

		Preferences prefs = Preferences.userRoot()
									   .node("modelthings");

		var config = graph_.getGraphConfiguration();
		config._antialiasing = prefs.getBoolean(PREFS_ANTIALIASING, false);
		config._fractionalMetrics = prefs.getBoolean(PREFS_FRACTIONAL_METRICS, false);

		antialiasingMenuCheck.setSelected(graph_.getGraphConfiguration()._antialiasing);
		fractionalMetricsMenuCheck.setSelected(graph_.getGraphConfiguration()._fractionalMetrics);

		antialiasingMenuCheck.addItemListener(e ->
		{
			updateGraphOption();
		});
		fractionalMetricsMenuCheck.addItemListener(e ->
		{
			updateGraphOption();
		});


		JMenu file = new JMenu("File", false);
		file.setMnemonic('F');
		file.add(loadMenuItem);
		menu.add(file);

		JMenu view = new JMenu("View", false);
		view.setMnemonic('V');
		view.add(antialiasingMenuCheck);
		view.add(fractionalMetricsMenuCheck);
		menu.add(view);

		setJMenuBar(menu);

		addWindowListener(new WindowAdapter()
		{
			@Override
			public void windowClosing(WindowEvent e)
			{
				Preferences prefs = Preferences.userRoot()
											   .node("modelthings");
				if (lastScxmlFile != null)
					prefs.put(PREFS_LAST_FILE, lastScxmlFile.toString());
				else
					prefs.remove(PREFS_LAST_FILE);
				prefs.putBoolean(PREFS_ANTIALIASING, graph_.getGraphConfiguration()._antialiasing);
				prefs.putBoolean(PREFS_FRACTIONAL_METRICS, graph_.getGraphConfiguration()._fractionalMetrics);

				try
				{
					prefs.flush();
				}
				catch (BackingStoreException ex)
				{
					ex.printStackTrace();
				}
			}
		});

		String l = prefs.get(PREFS_LAST_FILE, null);
		if (l != null && !l.isEmpty())
		{
			lastScxmlFile = Paths.get(l);
		}
		if (lastScxmlFile != null && Files.exists(lastScxmlFile))
		{
			openFsm(lastScxmlFile);
		}
	}

	protected void updateGraphOption()
	{
		graph_.getGraphConfiguration()._antialiasing = antialiasingMenuCheck.isSelected();
		graph_.getGraphConfiguration()._fractionalMetrics = fractionalMetricsMenuCheck.isSelected();

		graph_.repaint();

	}


	public static synchronized JFileChooser getSCXMLFileChooser()
	{
		if (scxmlFileChooser == null)
		{
			scxmlFileChooser = new JFileChooser();
			scxmlFileChooser.setFileFilter(new FileNameExtensionFilter("State Chart XML (SCXML) files", "scxml", "xml"));
			scxmlFileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		return scxmlFileChooser;
	}


	public static void main(String[] args)
	{
		Editor editor = new Editor("FSM Editor");
		editor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		editor.pack();
		editor.setLocationByPlatform(true);
		editor.setVisible(true);
	}

	public void openFsm(Path file)
	{

		try
		{
			XmlParser parser = new XmlParser();
			parser.addExtensionParser("*", new LogExtensionParser());

			ScxmlGraphExtension ge = new ScxmlGraphExtension();
			parser.addExtensionParser(ScxmlGraphExtension.NS_GRAPH_EXTENSION, ge);

			final FiniteStateMachine fsm = parser.parse(file, new String(Files.readAllBytes(file), StandardCharsets.UTF_8));

			graph_.setStateMachine(fsm, ge);
			lastScxmlFile = file;


		}
		catch (Exception e)
		{
			// @TODO
			e.printStackTrace();
		}
	}


}

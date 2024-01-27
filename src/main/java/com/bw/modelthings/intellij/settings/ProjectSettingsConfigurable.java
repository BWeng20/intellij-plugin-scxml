package com.bw.modelthings.intellij.settings;

import com.bw.modelthings.intellij.ScXmlSdkBundle;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.components.JBCheckBox;
import com.intellij.util.ui.FormBuilder;
import org.jetbrains.annotations.Nullable;

import javax.swing.JComponent;
import javax.swing.JPanel;

/**
 * Configurable implementation for EP com.intellij.projectConfigurable
 */
public class ProjectSettingsConfigurable implements com.intellij.openapi.options.Configurable
{
	private com.intellij.openapi.project.Project _theProject;

	/**
	 * Creates a new instance. Called by the framework.
	 *
	 * @param project The project.
	 */
	public ProjectSettingsConfigurable(com.intellij.openapi.project.Project project)
	{
		_theProject = project;
	}


	@Override
	public String getDisplayName()
	{
		return "SCXML SDK";
	}

	private JPanel _editorComponent;
	private JBCheckBox _antialiasing;
	private JBCheckBox _buffered;
	private JBCheckBox _zoomByMouseWheel;

	private ComboBox _editorLayout;

	@Override
	public @Nullable JComponent createComponent()
	{
		if (_editorComponent == null)
		{
			_antialiasing = new JBCheckBox(ScXmlSdkBundle.message("settings.antialiasing"));
			_buffered = new JBCheckBox(ScXmlSdkBundle.message("settings.states.buffered"));
			_zoomByMouseWheel = new JBCheckBox(ScXmlSdkBundle.message("settings.zoomByCtrlKey"));
			_editorLayout = new ComboBox(EditorLayout.values());


			_editorComponent = FormBuilder.createFormBuilder()
										  .addLabeledComponent(ScXmlSdkBundle.message("settings.editorLayoutLabel"), _editorLayout, 1)
										  .addComponent(_antialiasing, 1)
										  .addComponent(_buffered, 1)
										  .addComponent(_zoomByMouseWheel, 1)
										  .addComponentFillVertically(new JPanel(), 0)
										  .getPanel();
		}
		return _editorComponent;
	}

	@Override
	public boolean isModified()
	{
		PersistenceService service = _theProject.getService(PersistenceService.class);
		if (service != null)
		{
			Configuration configuration = service.getState();
			return configuration._buffered != _buffered.isSelected() ||
					configuration._antialiasing != _antialiasing.isSelected() ||
					configuration._zoomByMetaMouseWheelEnabled != _zoomByMouseWheel.isSelected() ||
					configuration._editorLayout != _editorLayout.getSelectedItem();
		}
		return false;
	}

	@Override
	public void apply() throws ConfigurationException
	{
		PersistenceService service = _theProject.getService(PersistenceService.class);
		if (service == null)
			throw new ConfigurationException("Persistence Service not available");

		if (isModified())
		{
			Configuration configuration = service.getState();
			configuration._buffered = _buffered.isSelected();
			configuration._antialiasing = _antialiasing.isSelected();
			configuration._zoomByMetaMouseWheelEnabled = _zoomByMouseWheel.isSelected();
			configuration._editorLayout = (EditorLayout) _editorLayout.getSelectedItem();

			ChangeConfigurationNotifier publisher = _theProject.getMessageBus()
															   .syncPublisher(ChangeConfigurationNotifier.CHANGE_CONFIG_TOPIC);
			publisher.onChange(configuration);
		}
	}

	@Override
	public void reset()
	{
		PersistenceService service = _theProject.getService(PersistenceService.class);
		if (service != null)
		{
			Configuration configuration = service.getState();
			_buffered.setSelected(configuration._buffered);
			_antialiasing.setSelected(configuration._antialiasing);
			_zoomByMouseWheel.setSelected(configuration._zoomByMetaMouseWheelEnabled);
			_editorLayout.setSelectedItem(configuration._editorLayout);
		}
	}

	@Override
	public void cancel()
	{
	}
}

package com.bw.modeldrive.settings;

import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.SettingsCategory;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Persistence service. Used by Configuration to store settings.
 */
@Service(Service.Level.PROJECT)
@com.intellij.openapi.components.State(
		name = "com.bw.modeldrive.settings.State",
		storages = {@Storage("scxmlSettings.xml")},
		category = SettingsCategory.PLUGINS
)
public final class PersistenceService implements PersistentStateComponent<Configuration>
{
	private Configuration configuration = new Configuration();

	private final Project theProject;

	/**
	 * Creates a new Service, called by framework.
	 *
	 * @param project The project to create the service for.
	 */
	public PersistenceService(Project project)
	{
		this.theProject = project;
	}

	@Nullable
	@Override
	public Configuration getState()
	{
		return configuration;
	}

	@Override
	public void loadState(@NotNull Configuration configuration)
	{
		this.configuration = configuration;
	}
}

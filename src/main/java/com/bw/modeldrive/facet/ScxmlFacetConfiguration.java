package com.bw.modeldrive.facet;

import com.intellij.facet.FacetConfiguration;
import com.intellij.facet.ui.FacetEditorContext;
import com.intellij.facet.ui.FacetEditorTab;
import com.intellij.facet.ui.FacetValidatorsManager;
import com.intellij.openapi.components.PersistentStateComponent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Configuration class for {@link ScxmlFacet}.
 */
public class ScxmlFacetConfiguration implements FacetConfiguration, PersistentStateComponent<ScxmlFacetState>
{

	// Manages the data stored with this facet.
	private ScxmlFacetState facetState = new ScxmlFacetState();

	/**
	 * Called by the IntelliJ Platform when saving this facet's state persistently.
	 *
	 * @return a component state. All properties, public and annotated fields are serialized.
	 * Only values which differ from default (i.e. the value of newly instantiated class) are serialized.
	 * {@code null} value indicates that the returned state won't be stored, and
	 * as a result previously stored state will be used.
	 */
	@Nullable
	@Override
	public ScxmlFacetState getState()
	{
		return facetState;
	}

	/**
	 * Called by the IntelliJ Platform when this facet's state is loaded.
	 * The method can and will be called several times, if config files were externally changed while IDEA running.
	 */
	@Override
	public void loadState(@NotNull ScxmlFacetState state)
	{
		facetState = state;
	}

	/**
	 * Creates a set of editor tabs for this facet, potentially one per context.
	 *
	 * @param context The context in which a facet is being added/deleted, or modified.
	 * @param manager The manager which can be used to access custom validators.
	 * @return Array of {@link ScxmlFacetEditorTab}. In this case size is always 1.
	 */
	@Override
	public FacetEditorTab[] createEditorTabs(FacetEditorContext context, FacetValidatorsManager manager)
	{
		return new FacetEditorTab[]{
				new ScxmlFacetEditorTab(facetState, context, manager)
		};
	}

}
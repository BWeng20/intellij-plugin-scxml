package com.bw.modeldrive.facet;

import org.jetbrains.annotations.NotNull;

/**
 * State for {@link ScxmlFacet}.
 */
public class ScxmlFacetState
{

	static final String FACET_INIT_PATH = "";

	public String pathToSdk;

	ScxmlFacetState()
	{
		setDemoFacetState(FACET_INIT_PATH);
	}

	@NotNull
	public String getDemoFacetState()
	{
		return pathToSdk;
	}

	public void setDemoFacetState(@NotNull String newPath)
	{
		pathToSdk = newPath;
	}

}

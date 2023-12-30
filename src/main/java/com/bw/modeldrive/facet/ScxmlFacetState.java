package com.bw.modeldrive.facet;

import org.jetbrains.annotations.NotNull;

/**
 * State for {@link ScxmlFacet}.
 */
public class ScxmlFacetState
{

	static final String FACET_INIT_PATH = "";

	/**
	 * Dummy.
	 */
	public String pathToSdk;

	ScxmlFacetState()
	{
		setScxmlSdkPath(FACET_INIT_PATH);
	}

	/**
	 * Currently a Dummy.
	 *
	 * @return The state.
	 */
	@NotNull
	public String getScxmlSdkPath()
	{
		return pathToSdk;
	}

	/**
	 * Currently a Dummy.
	 *
	 * @param newPath Dummy
	 */
	public void setScxmlSdkPath(@NotNull String newPath)
	{
		pathToSdk = newPath;
	}

}

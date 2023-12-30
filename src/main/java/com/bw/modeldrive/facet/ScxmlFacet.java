package com.bw.modeldrive.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;

/**
 * Facet class.
 */
public class ScxmlFacet extends Facet<ScxmlFacetConfiguration>
{
	/**
	 * Creates a new facet.
	 *
	 * @param facetType       The type
	 * @param module          The modul
	 * @param name            The name
	 * @param configuration   The configuration
	 * @param underlyingFacet The underlying facet
	 */
	public ScxmlFacet(FacetType facetType,
					  Module module,
					  String name,
					  ScxmlFacetConfiguration configuration,
					  Facet underlyingFacet)
	{
		super(facetType, module, name, configuration, underlyingFacet);
	}

}
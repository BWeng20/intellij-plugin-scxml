package com.bw.modeldrive.facet;

import com.intellij.facet.Facet;
import com.intellij.facet.FacetType;
import com.intellij.openapi.module.Module;

/**
 * Facet class.
 */
public class ScxmlFacet extends Facet<ScxmlFacetConfiguration>
{

	public ScxmlFacet(FacetType facetType,
					  Module module,
					  String name,
					  ScxmlFacetConfiguration configuration,
					  Facet underlyingFacet)
	{
		super(facetType, module, name, configuration, underlyingFacet);
	}

}
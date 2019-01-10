/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.extensions.rest.resource.terms.wiring;

import static org.ops4j.peaberry.Peaberry.service;

import javax.inject.Named;

import com.google.inject.multibindings.MapBinder;

import com.elasticpath.extensions.rest.resource.terms.permissions.TermsIdParameterStrategy;
import com.elasticpath.rest.authorization.parameter.PermissionParameterStrategy;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormResource;
import com.elasticpath.rest.helix.api.AbstractHelixModule;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;

/**
 * Class ServiceWiring.
 */
@Named
public class ServiceWiring extends AbstractHelixModule {
	@Override
	protected String resourceName() {
		return TermsAndConditionsFormResource.FAMILY;
	}

	@Override
	protected void configurePrototypes() {
		bind(CartOrderRepository.class).toProvider(service(CartOrderRepository.class).single());
	}

	@Override
	public void registerParameterResolvers(final MapBinder<String, PermissionParameterStrategy> resolvers) {
		super.registerParameterResolvers(resolvers);
		resolvers.addBinding(TermsAndConditionsFormIdentifier.TERMS_ID).toInstance(new TermsIdParameterStrategy());
	}
}

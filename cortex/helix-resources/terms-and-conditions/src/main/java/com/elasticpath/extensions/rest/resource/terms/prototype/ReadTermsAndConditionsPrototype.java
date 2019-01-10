/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.extensions.rest.resource.terms.prototype;

import javax.inject.Inject;

import io.reactivex.Single;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.terms.TermsAndConditionsEntity;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormResource;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype to Read.
 */
public class ReadTermsAndConditionsPrototype implements TermsAndConditionsFormResource.Read {
	@Inject
	@RequestIdentifier
	private TermsAndConditionsFormIdentifier termsAndConditionsFormIdentifier;

	@Inject
	@ResourceRepository
	private Repository<TermsAndConditionsEntity, TermsAndConditionsFormIdentifier> repository;

	@Override
	public Single<TermsAndConditionsEntity> onRead() {
		return repository.findOne(termsAndConditionsFormIdentifier);
	}
}

/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.extensions.rest.resource.terms.prototype;

import javax.inject.Inject;

import io.reactivex.Completable;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.terms.TermsAndConditionsEntity;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormResource;
import com.elasticpath.rest.helix.data.annotation.RequestForm;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceRepository;

/**
 * Prototype for Update.
 */
public class UpdateTermsAndConditionsPrototype implements TermsAndConditionsFormResource.Update {
	@Inject
	@RequestForm
	private TermsAndConditionsEntity termsEntityForm;

	@Inject
	@RequestIdentifier
	private TermsAndConditionsFormIdentifier termsAndConditionsFormIdentifier;

	@Inject
	@ResourceRepository
	private Repository<TermsAndConditionsEntity, TermsAndConditionsFormIdentifier> repository;

	@Override
	public Completable onUpdate() {
		return repository.update(termsEntityForm, termsAndConditionsFormIdentifier);
	}
}

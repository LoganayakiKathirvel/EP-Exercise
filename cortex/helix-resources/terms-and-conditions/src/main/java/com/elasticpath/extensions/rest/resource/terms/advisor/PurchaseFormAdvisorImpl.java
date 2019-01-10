/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.extensions.rest.resource.terms.advisor;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.extensions.rest.resource.integration.epcommerce.repository.TermsValidationService;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.purchases.CreatePurchaseFormIdentifier;
import com.elasticpath.rest.definition.terms.PurchaseFormTermsAdvisor;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;

/**
 * PurchaseFormAdvisorImpl class.
 */
public class PurchaseFormAdvisorImpl implements PurchaseFormTermsAdvisor.LinkedFormAdvisor {

	@Inject
	@RequestIdentifier
	private CreatePurchaseFormIdentifier createPurchaseFormIdentifier;

	@Inject
	@ResourceService
	private TermsValidationService validationService;

	@Override
	public Observable<LinkedMessage<TermsAndConditionsFormIdentifier>> onLinkedAdvise() {
		return validationService.validateTermsAccepted(createPurchaseFormIdentifier.getOrder());
	}
}

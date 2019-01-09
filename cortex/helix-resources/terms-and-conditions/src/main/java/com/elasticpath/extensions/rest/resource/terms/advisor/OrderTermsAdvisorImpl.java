/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.rest.resource.terms.advisor;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.extensions.rest.resource.integration.epcommerce.repository.TermsValidationService;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.terms.OrderTermsAdvisor;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;
import com.elasticpath.rest.helix.data.annotation.ResourceService;

/**
 * Terms advisor on orders resource.
 */
public final class OrderTermsAdvisorImpl implements OrderTermsAdvisor.ReadLinkedAdvisor {

	@Inject
	@RequestIdentifier
	private OrderIdentifier orderIdentifier;

	@Inject
	@ResourceService
	private TermsValidationService validationService;

	@Override
	public Observable<LinkedMessage<TermsAndConditionsFormIdentifier>> onLinkedAdvise() {
		return validationService.validateTermsAccepted(orderIdentifier);
	}
}

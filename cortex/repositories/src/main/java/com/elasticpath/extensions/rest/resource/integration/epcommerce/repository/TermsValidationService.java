/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.rest.resource.integration.epcommerce.repository;

import io.reactivex.Observable;

import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;

/**
 * Validation service for checking if the terms have been accepted.
 */
public interface TermsValidationService {

	/**
	 * Check if the terms have been accepted.
	 *
	 * @param orderIdentifier orderIdentifier
	 * @return a message if terms not accepted
	 */
	Observable<LinkedMessage<TermsAndConditionsFormIdentifier>> validateTermsAccepted(OrderIdentifier orderIdentifier);
}
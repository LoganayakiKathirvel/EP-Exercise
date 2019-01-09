/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.rest.resource.terms.relationship;

import javax.inject.Inject;

import io.reactivex.Observable;

import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.terms.OrderToTermsRelationship;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.helix.data.annotation.RequestIdentifier;

/**
 * Implementation of link-to operation of {@link OrderToTermsRelationship}.
 */
public class OrderToTermsRelationshipImpl implements OrderToTermsRelationship.LinkTo {

	@Inject
	@RequestIdentifier
	private OrderIdentifier orderIdentifier;

	@Override
	public Observable<TermsAndConditionsFormIdentifier> onLinkTo() {
		return Observable.just(TermsAndConditionsFormIdentifier.builder()
				.withScope(orderIdentifier.getScope())
				.withTermsId(orderIdentifier.getOrderId())
				.build());
	}
}

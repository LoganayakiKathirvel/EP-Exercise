/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.rest.resource.terms.relationship;

import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class OrderToTermsRelationshipImplTest {


	private static final IdentifierPart<String> SCOPE = StringIdentifier.of("scope");
	private static final IdentifierPart<String> ORDER_ID = StringIdentifier.of("orderId");

	@Mock
	private OrderIdentifier orderIdentifier;

	@InjectMocks
	private OrderToTermsRelationshipImpl fixture;


	@Test
	public void shouldReturnLinkToTerms() {
		when(orderIdentifier.getScope()).thenReturn(SCOPE);
		when(orderIdentifier.getOrderId()).thenReturn(ORDER_ID);

		TermsAndConditionsFormIdentifier expectedIdentifier = TermsAndConditionsFormIdentifier.builder()
				.withScope(SCOPE)
				.withTermsId(ORDER_ID)
				.build();

		fixture.onLinkTo()
				.test()
				.assertValue(expectedIdentifier);
	}

}

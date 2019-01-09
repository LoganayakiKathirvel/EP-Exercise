/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.rest.resource.terms.permissions;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;

import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;
import io.reactivex.Observable;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.id.Identifier;
import com.elasticpath.rest.id.transform.IdentifierTransformer;
import com.elasticpath.rest.id.transform.IdentifierTransformerProvider;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.id.util.Base32Util;
import com.elasticpath.rest.identity.ScopePrincipal;
import com.elasticpath.rest.identity.UserPrincipal;

@RunWith(MockitoJUnitRunner.class)
public class TermsIdParameterStrategyTest {

	private static final String TEST_REALM = "testRealm";
	private static final String DECODED_TERMS_ID = "7F4E992F-9CFC-E648-BA11-DF1D5B23968F";


	@InjectMocks
	private TermsIdParameterStrategy fixture;

	@Mock
	CartOrderRepository cartOrderRepository;

	@Mock
	private IdentifierTransformerProvider identifierTransformerProvider;
	@Mock
	private IdentifierTransformer<Identifier> identifierTransformer;

	/**
	 * Test get terms id parameter.
	 */
	@Test
	public void testGetTermsIdParameterValue() {
		final Identifier termsIdentifier = StringIdentifier.of(DECODED_TERMS_ID);
		PrincipalCollection principals = new SimplePrincipalCollection(
				new SimplePrincipalCollection(
						Arrays.asList(
								new UserPrincipal(DECODED_TERMS_ID),
								new ScopePrincipal(TEST_REALM)), "test-realm")
		);
		when(cartOrderRepository.findCartOrderGuidsByCustomerAsObservable(TEST_REALM, DECODED_TERMS_ID))
				.thenReturn(Observable.just(DECODED_TERMS_ID));
		when(identifierTransformerProvider.forUriPart(TermsAndConditionsFormIdentifier.TERMS_ID)).thenReturn(identifierTransformer);
		when(identifierTransformer.identifierToUri(any())).thenReturn(Base32Util.encode(DECODED_TERMS_ID));

		String expectedTermsIds = Base32Util.encode(DECODED_TERMS_ID);
		String termsIds = fixture.getParameterValue(principals);

		assertEquals(expectedTermsIds, termsIds);
		verify(identifierTransformerProvider).forUriPart(TermsAndConditionsFormIdentifier.TERMS_ID);
		verify(identifierTransformer).identifierToUri(termsIdentifier);

	}

}

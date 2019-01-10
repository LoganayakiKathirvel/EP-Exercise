/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.extensions.rest.resource.terms.prototype;

import static org.mockito.Mockito.verify;

import org.junit.Test;
import org.junit.runner.RunWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;

/**
 * Test class for {@link ReadTermsAndConditionsPrototype}.
 */
@RunWith(MockitoJUnitRunner.class)
public class ReadTermsAndConditionsPrototypeTest {

	@InjectMocks
	private ReadTermsAndConditionsPrototype readTermsAndConditionsPrototype;

	@Mock
	private Repository repository;

	@Mock
	private TermsAndConditionsFormIdentifier termsAndConditionsFormIdentifier;

	@Test
	public void shouldReturnTermsAndConditions() {
		readTermsAndConditionsPrototype.onRead();

		verify(repository).findOne(termsAndConditionsFormIdentifier);
	}

}
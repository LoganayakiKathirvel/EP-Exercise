/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.rest.resource.terms.prototype;


import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.reactivex.Completable;
import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.terms.TermsAndConditionsEntity;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;


/**
 * Test class for {@link ReadTermsAndConditionsPrototype}.
 */
@RunWith(MockitoJUnitRunner.class)
public class UpdateTermsAndConditionsPrototypeTest {

	@InjectMocks
	private UpdateTermsAndConditionsPrototype updateTermsAndConditionsPrototype;

	@Mock
	private Repository repository;

	@Mock
	private TermsAndConditionsFormIdentifier termsAndConditionsFormIdentifier;

	@Mock
	private TermsAndConditionsEntity termsAndConditionsEntity;


	@Test
	public void shouldUpdateTermsAndConditionsFromEntity() {
		when(repository.update(termsAndConditionsEntity, termsAndConditionsFormIdentifier))
				.thenReturn(Single.just("this").toCompletable());

		assertThat(updateTermsAndConditionsPrototype.onUpdate(), instanceOf(Completable.class));
		verify(repository).update(termsAndConditionsEntity, termsAndConditionsFormIdentifier);

	}

}
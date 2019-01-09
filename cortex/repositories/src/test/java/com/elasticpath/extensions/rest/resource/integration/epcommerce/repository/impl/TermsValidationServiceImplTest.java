/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.rest.resource.integration.epcommerce.repository.impl;

//import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import io.reactivex.Single;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.repository.Repository;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.terms.TermsAndConditionsEntity;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.id.type.StringIdentifier;

@RunWith(MockitoJUnitRunner.class)
public class TermsValidationServiceImplTest {

    private final IdentifierPart<String> orderId = StringIdentifier.of("orderId");

    private final IdentifierPart<String> scopeId = StringIdentifier.of("scope");

    @Mock
    private Repository<TermsAndConditionsEntity, TermsAndConditionsFormIdentifier> repository;

    @Mock
    private OrderIdentifier orderIdentifier;

    @InjectMocks
    private TermsValidationServiceImpl fixture;

    @Test
    public void shouldReturnLinkToTermsWhenTermNotAccepted() {
        Boolean termsAccepted = false;

        when(orderIdentifier.getScope()).thenReturn(scopeId);
        when(orderIdentifier.getOrderId()).thenReturn(orderId);

        TermsAndConditionsEntity mockEntity = TermsAndConditionsEntity.builder()
                .withAccepted(termsAccepted)
                .withMessage("message")
                .build();

        when(repository.findOne(any(TermsAndConditionsFormIdentifier.class))).thenReturn(Single.just(mockEntity));

        fixture.validateTermsAccepted(orderIdentifier)
                .test()
                .assertNoErrors()
                .assertValueCount(1);
    }

    @Test
    public void shouldReturnNoLinksWhenTermsAccepted() {
        Boolean termsAccepted = true;

        when(orderIdentifier.getScope()).thenReturn(scopeId);
        when(orderIdentifier.getOrderId()).thenReturn(orderId);

        TermsAndConditionsEntity mockEntity = TermsAndConditionsEntity.builder()
                .withAccepted(termsAccepted)
                .withMessage("message")
                .build();

        when(repository.findOne(any(TermsAndConditionsFormIdentifier.class))).thenReturn(Single.just(mockEntity));

        fixture.validateTermsAccepted(orderIdentifier)
                .test()
                .assertNoErrors()
                .assertValueCount(0);
    }
}
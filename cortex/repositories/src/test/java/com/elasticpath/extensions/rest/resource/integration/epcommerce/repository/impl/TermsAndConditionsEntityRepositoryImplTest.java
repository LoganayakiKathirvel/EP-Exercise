/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.rest.resource.integration.epcommerce.repository.impl;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Map;

import io.reactivex.Single;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.elasticpath.extensions.domain.cartorder.ExtCartOrder;
import com.elasticpath.extensions.domain.termsandconditions.CartOrderTermsAndConditionsFlag;
import com.elasticpath.rest.definition.terms.TermsAndConditionsEntity;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.id.type.StringIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;

@RunWith(MockitoJUnitRunner.class)
public class TermsAndConditionsEntityRepositoryImplTest {
    private static final String SCOPE = "SCOPE";
    private static final String TERMS_ID = "TERMS_ID";
    private static final String TERMS_KEY = "generalTerms";

    @InjectMocks
    private TermsAndConditionsEntityRepositoryImpl<TermsAndConditionsEntity, TermsAndConditionsFormIdentifier> repository;

    @Mock
    private CartOrderRepository cartOrderRepository;

    @Mock
    private ExtCartOrder cartOrder;

    @Mock
    private CartOrderTermsAndConditionsFlag cartOrderTermsAndConditionsFlag;

    @Before
    public void setup() {
        Map termsAndConditionsFlagMap = mock(Map.class);

        when(cartOrderRepository.findByGuidAsSingle(any(String.class), any(String.class))).thenReturn(Single.just(cartOrder));
        when(termsAndConditionsFlagMap.get(TERMS_KEY)).thenReturn(cartOrderTermsAndConditionsFlag);
        when(cartOrderTermsAndConditionsFlag.isAccepted()).thenReturn(true);
        when(cartOrder.getCartOrderTermsAndConditionsFlags()).thenReturn(termsAndConditionsFlagMap);
        when(termsAndConditionsFlagMap.containsKey(TERMS_KEY)).thenReturn(true);
        when(cartOrderRepository.saveCartOrderAsSingle(cartOrder)).thenReturn(Single.just(cartOrder));
    }

    @Test
    public void testUpdate() {
        TermsAndConditionsFormIdentifier identifier = getTermsAndConditionsFormIdentifier();

        TermsAndConditionsEntity entity = TermsAndConditionsEntity.builder().withAccepted(true).build();

        repository.update(entity, identifier)
                .test()
                .assertNoErrors()
                .assertComplete();
    }

    @Test
    public void testFindOne() {
        TermsAndConditionsFormIdentifier identifier = getTermsAndConditionsFormIdentifier();

        repository.findOne(identifier)
                .test()
                .assertNoErrors()
                .assertValue(TermsAndConditionsEntity.builder()
                        .withAccepted(true)
                        .withMessage("Do you accept the Terms and Conditions?").build());
    }

    private TermsAndConditionsFormIdentifier getTermsAndConditionsFormIdentifier() {
        return TermsAndConditionsFormIdentifier.builder()
                .withTermsId(StringIdentifier.of(TERMS_ID))
                .withScope(StringIdentifier.of(SCOPE))
                .build();
    }
}
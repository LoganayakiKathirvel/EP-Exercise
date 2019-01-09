/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.rest.resource.integration.epcommerce.repository.impl;

import io.reactivex.Observable;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.extensions.rest.resource.integration.epcommerce.repository.TermsValidationService;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.advise.LinkedMessage;
import com.elasticpath.rest.definition.orders.OrderIdentifier;
import com.elasticpath.rest.definition.terms.TermsAndConditionsEntity;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.id.IdentifierPart;
import com.elasticpath.rest.schema.StructuredMessageTypes;

/**
 * Terms Validation Service Impl.
 */
@Component
public final class TermsValidationServiceImpl implements TermsValidationService {

    @Reference(target = "(name=termsAndConditionsEntityRepositoryImpl)")
    private Repository<TermsAndConditionsEntity, TermsAndConditionsFormIdentifier> repository;

    @Override
    public Observable<LinkedMessage<TermsAndConditionsFormIdentifier>> validateTermsAccepted(final OrderIdentifier orderIdentifier) {
        IdentifierPart<String> scope = orderIdentifier.getScope();
        IdentifierPart<String> orderId = orderIdentifier.getOrderId();

        TermsAndConditionsFormIdentifier termsAndConditionsFormIdentifier = TermsAndConditionsFormIdentifier.builder()
                .withScope(scope)
                .withTermsId(orderId)
                .build();

        return repository.findOne(termsAndConditionsFormIdentifier)
                .flatMapObservable(termsAndConditionsEntity -> shouldAddLink(scope, orderId, termsAndConditionsEntity))
                .map(this::buildLinkedMessage);
    }

    private Observable<TermsAndConditionsFormIdentifier> shouldAddLink(final IdentifierPart<String> scope,
                                                                       final IdentifierPart<String> orderId,
                                                                       final TermsAndConditionsEntity termsAndConditionsEntity) {
        if (termsAndConditionsEntity.isAccepted()) {
            return Observable.empty();
        }
        return Observable.just(TermsAndConditionsFormIdentifier.builder()
                .withScope(scope)
                .withTermsId(orderId)
                .build());
    }

    private LinkedMessage<TermsAndConditionsFormIdentifier> buildLinkedMessage(
            final TermsAndConditionsFormIdentifier termsAndConditionsFormIdentifier) {

        return LinkedMessage.<TermsAndConditionsFormIdentifier>builder()
                .withType(StructuredMessageTypes.NEEDINFO)
                .withDebugMessage("Terms must be accepted before making a purchase.")
                .withId("terms.not.accepted")
                .withLinkedIdentifier(termsAndConditionsFormIdentifier)
                .build();
    }
}
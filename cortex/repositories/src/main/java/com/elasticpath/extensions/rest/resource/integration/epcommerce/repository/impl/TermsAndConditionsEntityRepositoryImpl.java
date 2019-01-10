/*
 * Copyright © 2016 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.extensions.rest.resource.integration.epcommerce.repository.impl;

import java.util.Map;

import io.reactivex.Completable;
import io.reactivex.Single;

import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.extensions.domain.cartorder.ExtCartOrder;
import com.elasticpath.extensions.domain.termsandconditions.CartOrderTermsAndConditionsFlag;
import com.elasticpath.extensions.domain.termsandconditions.impl.CartOrderTermsAndConditionsFlagImpl;
import com.elasticpath.repository.Repository;
import com.elasticpath.rest.cache.CacheResult;
import com.elasticpath.rest.definition.terms.TermsAndConditionsEntity;
import com.elasticpath.rest.definition.terms.TermsAndConditionsFormIdentifier;
import com.elasticpath.rest.resource.integration.epcommerce.repository.cartorder.CartOrderRepository;

/**
 * Terms and Conditions Entity Repository.
 *
 * @param <E> the entity type
 * @param <I> the identifier type
 */
@Component(property = "name=termsAndConditionsEntityRepositoryImpl")
public class TermsAndConditionsEntityRepositoryImpl<E extends TermsAndConditionsEntity, I extends TermsAndConditionsFormIdentifier>
		implements Repository<TermsAndConditionsEntity, TermsAndConditionsFormIdentifier> {

	private static final String TERMS_KEY = "generalTerms";

	@Reference
	private CartOrderRepository cartOrderRepository;

	@Override
	public Completable update(final TermsAndConditionsEntity entity, final TermsAndConditionsFormIdentifier identifier) {
		return getOrder(identifier)
				.flatMap(order -> updateOrder(entity, order))
				.toCompletable();
	}

	private Single<CartOrder> updateOrder(final TermsAndConditionsEntity entity, final ExtCartOrder cartOrder) {

		Map<String, CartOrderTermsAndConditionsFlag> cartOrderTermsAndConditionsFlags = cartOrder.getCartOrderTermsAndConditionsFlags();
		CartOrderTermsAndConditionsFlag generalFlag;
		if (cartOrderTermsAndConditionsFlags.containsKey(TERMS_KEY)) {
			generalFlag = cartOrderTermsAndConditionsFlags.get(TERMS_KEY);

		} else {
			generalFlag = new CartOrderTermsAndConditionsFlagImpl();
			generalFlag.setCode(TERMS_KEY);
		}

		generalFlag.setAccepted(entity.isAccepted());
		cartOrderTermsAndConditionsFlags.put(TERMS_KEY, generalFlag);
		cartOrder.setCartOrderTermsAndConditionsFlags(cartOrderTermsAndConditionsFlags);
		return cartOrderRepository.saveCartOrderAsSingle(cartOrder);
	}

	@Override
	@CacheResult
	public Single<TermsAndConditionsEntity> findOne(final TermsAndConditionsFormIdentifier identifier) {
		return getOrder(identifier).map(this::isCartOrderTermsAndConditionsAccepted)
				.map(isAccepted -> TermsAndConditionsEntity.builder()
						.withAccepted(isAccepted)
						.withMessage("Do you accept the Terms and Conditions?").build());
	}

	private boolean isCartOrderTermsAndConditionsAccepted(final ExtCartOrder cartOrder) {
		return cartOrder.getCartOrderTermsAndConditionsFlags().containsKey(TERMS_KEY)
				&& cartOrder.getCartOrderTermsAndConditionsFlags().get(TERMS_KEY).isAccepted();
	}

	private Single<ExtCartOrder> getOrder(final TermsAndConditionsFormIdentifier identifier) {
		ExtCartOrder cartOrder = (ExtCartOrder) cartOrderRepository.findByGuidAsSingle(identifier.getScope().getValue(),
				identifier.getTermsId().getValue()).blockingGet();
		return Single.just(cartOrder);
	}
}
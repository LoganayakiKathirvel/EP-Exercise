/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.extensions.domain.cartorder;

import java.util.Map;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.extensions.domain.termsandconditions.CartOrderTermsAndConditionsFlag;

/**
 * Interface ExtCartOrder.
 */
public interface ExtCartOrder extends CartOrder {
	/**
	 * @return map of string and CartOrderTermsAndConditionsFlag
	 */
	Map<String, CartOrderTermsAndConditionsFlag> getCartOrderTermsAndConditionsFlags();

	/**
	 * @param cartOrderTermsAndConditionsFlags map of string and CartOrderTermsAndConditionsFlag
	 */
	void setCartOrderTermsAndConditionsFlags(Map<String, CartOrderTermsAndConditionsFlag> cartOrderTermsAndConditionsFlags);

	/**
	 * @param cartOrderTermsAndConditionsFlag cartOrderTermsAndConditionsFlag
	 */
	void addCartOrderTermsAndConditionsFlag(CartOrderTermsAndConditionsFlag cartOrderTermsAndConditionsFlag);

	/**
	 * @param code code
	 */
	void removeCartOrderTermsAndConditionsFlag(String code);
}

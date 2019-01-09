package com.elasticpath.extensions.domain.cartorder;

import java.util.Map;

import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.extensions.domain.termsandconditions.CartOrderTermsAndConditionsFlag;

/**
 * Extension of CartOrder to add TnC acceptance flags.
 */
public interface ExtCartOrder extends CartOrder {

    /**
     * Gets the Map of TnC acceptance flags.
     *
     * @return Map of TnC acceptance flags
     */
    Map<String, CartOrderTermsAndConditionsFlag> getCartOrderTermsAndConditionsFlags();

    /**
     * Sets Map of TnC acceptance flags.
     *
     * @param cartOrderTermsAndConditionsFlags the Map of TnC acceptance flags
     */
    void setCartOrderTermsAndConditionsFlags(Map<String, CartOrderTermsAndConditionsFlag> cartOrderTermsAndConditionsFlags);

    /**
     * Adds a new TnC acceptance flag.
     *
     * @param cartOrderTermsAndConditionsFlag the TnC acceptance flag
     */
    void addCartOrderTermsAndConditionsFlag(CartOrderTermsAndConditionsFlag cartOrderTermsAndConditionsFlag);

    /**
     * Removes a TnC acceptance flag based on its code.
     *
     * @param code the TnC code
     */
    void removeCartOrderTermsAndConditionsFlag(String code);

}

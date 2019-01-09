package com.elasticpath.extensions.service.shoppingcart.impl;

import java.util.Map;
import java.util.Map.Entry;

import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.extensions.domain.cartorder.ExtCartOrder;
import com.elasticpath.service.shoppingcart.impl.OrderFactoryImpl;
import com.elasticpath.extensions.domain.termsandconditions.CartOrderTermsAndConditionsFlag;

/**
 * Extension to copy TnC acceptance flags to Order.
 */
public class ExtOrderFactoryImpl extends OrderFactoryImpl {

    @Override
    protected void fillInOrderDetails(
            final Order order,
            final ShoppingCart shoppingCart,
            final ShoppingCartTaxSnapshot pricingSnapshot,
            final CustomerSession customerSession,
            final Customer customer,
            final boolean isExchangeOrder,
            final boolean awaitExchangeCompletion) {

        super.fillInOrderDetails(order, shoppingCart, pricingSnapshot, customerSession, customer, isExchangeOrder, awaitExchangeCompletion);

        ExtCartOrder extCartOrder = (ExtCartOrder) getCartOrderService().findByShoppingCartGuid(shoppingCart.getGuid());

        if (extCartOrder != null) {
            Map<String, CartOrderTermsAndConditionsFlag> cartOrderTermsAndConditionsFlags = extCartOrder.getCartOrderTermsAndConditionsFlags();

            for (Entry<String, CartOrderTermsAndConditionsFlag> entry : cartOrderTermsAndConditionsFlags.entrySet()) {
                order.setFieldValue(entry.getValue().getCode(), Boolean.toString(entry.getValue().isAccepted()).toUpperCase());
            }
        }
        // Else we should log a warning.
    }
}

/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.extensions.termsandconditions;

import static org.junit.Assert.assertEquals;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.builder.checkout.CheckoutTestCartBuilder;
import com.elasticpath.domain.builder.customer.CustomerBuilder;
import com.elasticpath.domain.builder.shopper.ShoppingContext;
import com.elasticpath.domain.builder.shopper.ShoppingContextBuilder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.misc.CheckoutResults;
import com.elasticpath.domain.order.Order;
import com.elasticpath.domain.order.OrderPayment;
import com.elasticpath.domain.payment.PaymentGateway;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.ShoppingCartPricingSnapshot;
import com.elasticpath.domain.shoppingcart.ShoppingCartTaxSnapshot;
import com.elasticpath.domain.store.Store;
import com.elasticpath.extensions.domain.cartorder.ExtCartOrder;
import com.elasticpath.extensions.domain.termsandconditions.CartOrderTermsAndConditionsFlag;
import com.elasticpath.persister.Persister;
import com.elasticpath.plugin.payment.PaymentType;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.customer.CustomerService;
import com.elasticpath.service.payment.gateway.impl.NullPaymentGatewayPluginImpl;
import com.elasticpath.service.shoppingcart.CheckoutService;
import com.elasticpath.service.shoppingcart.PricingSnapshotService;
import com.elasticpath.service.shoppingcart.TaxSnapshotService;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;

/**
 * Test cases for checkout with TnC flags.
 */
public class CheckoutWithTnCFlagsTest extends BasicSpringContextTest {

    private static final String MYTNCCODE = "mytnccode";

    private static final String MYOTHERTNCCODE = "myothertnccode";

    @Autowired
    private CheckoutService checkoutService;

    @Autowired
    private CheckoutTestCartBuilder checkoutTestCartBuilder;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerBuilder customerBuilder;

    @Autowired
    private PricingSnapshotService pricingSnapshotService;

    @Autowired
    private TaxSnapshotService taxSnapshotService;

    @Autowired
    private ShoppingContextBuilder shoppingContextBuilder;

    @Autowired
    private Persister<ShoppingContext> shoppingContextPersister;

    @Autowired
    private CartOrderService cartOrderService;

    private ShoppingContext shoppingContext;

    private Customer testCustomer;

    /**
     * Set up common elements of the test.
     */
    @Before
    public void setUp() {
        SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);

        final Store store = scenario.getStore();
        store.setPaymentGateways(setUpPaymentGatewayAndProperties());

        final String storeCode = store.getCode();
        testCustomer = customerBuilder.withStoreCode(storeCode)
                .build();
        customerService.add(testCustomer);

        shoppingContext = shoppingContextBuilder
                .withCustomer(testCustomer)
                .withStoreCode(storeCode)
                .build();

        shoppingContextPersister.persist(shoppingContext);

        checkoutTestCartBuilder.withScenario(scenario)
                .withCustomerSession(shoppingContext.getCustomerSession());

        NullPaymentGatewayPluginImpl.setFailOnCapture(false);
        NullPaymentGatewayPluginImpl.setFailOnPreAuthorize(false);
        NullPaymentGatewayPluginImpl.setFailOnReversePreAuthorization(false);
        NullPaymentGatewayPluginImpl.setFailOnSale(false);
    }

    private OrderPayment getOrderPayment() {
       /* final OrderPayment orderPayment = getBeanFactory().getBean(ContextIdNames.ORDER_PAYMENT);
        orderPayment.setCardHolderName("test test");
        orderPayment.setCardType("001");
        orderPayment.setCreatedDate(new Date());
        orderPayment.setCurrencyCode("USD");
        orderPayment.setEmail(testCustomer.getEmail());
        orderPayment.setExpiryMonth("09");
        orderPayment.setExpiryYear("10");
        orderPayment.setPaymentMethod(PaymentType.CREDITCARD);
        orderPayment.setCvv2Code("1111");
        orderPayment.setUnencryptedCardNumber("4111111111111111");
        return orderPayment;*/
        final OrderPayment orderPayment = getBeanFactory().getBean(ContextIdNames.ORDER_PAYMENT);
        orderPayment.setCreatedDate(new Date());
        orderPayment.setCurrencyCode("USD");
        orderPayment.setEmail(testCustomer.getEmail());
        orderPayment.setPaymentMethod(PaymentType.PAYMENT_TOKEN);
        return orderPayment;
    }

    private Set<PaymentGateway> setUpPaymentGatewayAndProperties() {
        final Set<PaymentGateway> gateways = new HashSet<PaymentGateway>();
        gateways.add(getTac().getPersistersFactory().getStoreTestPersister().persistDefaultPaymentGateway());
        return gateways;
    }

    @DirtiesDatabase
    @Test
    public void testSuccessfulCheckoutWithNoAcceptanceFields() {
        OrderPayment templateOrderPayment = getOrderPayment();

        ShoppingCart shoppingCart = checkoutTestCartBuilder.withTestGateway()
                .withElectronicProduct()
                .build();

        final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
        final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

        CheckoutResults results =
                checkoutService.checkout(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(), templateOrderPayment, true);

        Order order = results.getOrder();

        Map<String, String> fieldValues = order.getFieldValues();

        assertEquals(0, fieldValues.size());
    }

    @DirtiesDatabase
    @Test
    public void testSuccessfulCheckoutWithAcceptanceFields() {
        OrderPayment templateOrderPayment = getOrderPayment();

        ShoppingCart shoppingCart = checkoutTestCartBuilder.withTestGateway()
                .withElectronicProduct()
                .build();

        final ShoppingCartPricingSnapshot pricingSnapshot = pricingSnapshotService.getPricingSnapshotForCart(shoppingCart);
        final ShoppingCartTaxSnapshot taxSnapshot = taxSnapshotService.getTaxSnapshotForCart(shoppingCart, pricingSnapshot);

        ExtCartOrder extCartOrder;

        extCartOrder = this.getBeanFactory().getBean(ContextIdNames.CART_ORDER);

        extCartOrder.setShoppingCartGuid(shoppingCart.getGuid());

        CartOrderTermsAndConditionsFlag cartOrderTermsAndConditionsFlag;

        cartOrderTermsAndConditionsFlag = this.getBeanFactory().getBean("cartOrderTermsAndConditionsFlag");
        cartOrderTermsAndConditionsFlag.setCode(MYTNCCODE);
        cartOrderTermsAndConditionsFlag.setAccepted(true);
        extCartOrder.addCartOrderTermsAndConditionsFlag(cartOrderTermsAndConditionsFlag);

        cartOrderTermsAndConditionsFlag = this.getBeanFactory().getBean("cartOrderTermsAndConditionsFlag");
        cartOrderTermsAndConditionsFlag.setCode(MYOTHERTNCCODE);
        cartOrderTermsAndConditionsFlag.setAccepted(true);
        extCartOrder.addCartOrderTermsAndConditionsFlag(cartOrderTermsAndConditionsFlag);

        this.cartOrderService.saveOrUpdate(extCartOrder);

        CheckoutResults results =
                checkoutService.checkout(shoppingCart, taxSnapshot, shoppingContext.getCustomerSession(), templateOrderPayment, true);

        Order order = results.getOrder();

        Map<String, String> fieldValues = order.getFieldValues();

        assertEquals(2, fieldValues.size());

        assertEquals(fieldValues.get(MYTNCCODE), "TRUE");

        assertEquals(fieldValues.get(MYOTHERTNCCODE), "TRUE");
    }
}
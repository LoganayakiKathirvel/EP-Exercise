/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */

package com.elasticpath.extensions.termsandconditions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.util.Currency;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;

import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.commons.constants.ContextIdNames;
import com.elasticpath.domain.cartorder.CartOrder;
import com.elasticpath.domain.customer.Customer;
import com.elasticpath.domain.customer.CustomerSession;
import com.elasticpath.domain.factory.TestCustomerSessionFactoryForTestApplication;
import com.elasticpath.domain.factory.TestShopperFactoryForTestApplication;
import com.elasticpath.domain.shopper.Shopper;
import com.elasticpath.domain.shoppingcart.ShoppingCart;
import com.elasticpath.domain.shoppingcart.impl.ShoppingCartImpl;
import com.elasticpath.domain.store.Store;
import com.elasticpath.extensions.domain.cartorder.ExtCartOrder;
import com.elasticpath.extensions.domain.termsandconditions.CartOrderTermsAndConditionsFlag;
import com.elasticpath.service.cartorder.CartOrderService;
import com.elasticpath.service.shopper.ShopperService;
import com.elasticpath.service.shoppingcart.ShoppingCartService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;
import com.elasticpath.test.persister.StoreTestPersister;
import com.elasticpath.test.persister.testscenarios.SimpleStoreScenario;
import com.elasticpath.test.util.Utils;

/**
 * Test cases for the CartOrderTermsAndConditionsFlag.
 */
public class CartOrderTermsAndConditionsTest extends BasicSpringContextTest {

    private static final String MYFALSETNCCODE = "myfalsetnccode";

    private static final String MYTRUETNCCODE = "mytruetnccode";

    @Autowired
    private BeanFactory beanFactory;

    @Autowired
    private CartOrderService cartOrderService;

    @Autowired
    private ShoppingCartService shoppingCartService;

    private ShoppingCart shoppingCart;

    private Store store;

    /**
     * Set up common elements of the test.
     */
    @Before
    public void setUp() {
        SimpleStoreScenario scenario = getTac().useScenario(SimpleStoreScenario.class);

        store = scenario.getStore();

        shoppingCart = configureAndPersistCart();
    }

    /**
     * Tests that we have the Spring prototypes correctly wired up.
     */
    @Test
    public void testExtendedCartOrder() {
        CartOrder cartOrder;

        cartOrder = this.beanFactory.getBean(ContextIdNames.CART_ORDER);

        assertTrue(cartOrder instanceof ExtCartOrder);
    }

    /**
     * Tests that we have a good extension class.
     */
    @Test
    @DirtiesDatabase
    public void testPersistedExtendedCartOrder() {

        CartOrder cartOrder;

        cartOrder = this.beanFactory.getBean(ContextIdNames.CART_ORDER);

        cartOrder.setShoppingCartGuid(shoppingCart.getGuid());

        CartOrder newCartOrder;

        String guid = cartOrder.getGuid();

        newCartOrder = this.cartOrderService.saveOrUpdate(cartOrder);

        newCartOrder = this.cartOrderService.findByShoppingCartGuid(shoppingCart.getGuid());

        assertNotNull(newCartOrder);

        assertTrue(newCartOrder instanceof ExtCartOrder);

        assertEquals(guid, newCartOrder.getGuid());
    }

    @Test
    @DirtiesDatabase
    public void testPersistedExtendedCartOrderWithTnCs() {

        ExtCartOrder extCartOrder;

        extCartOrder = this.beanFactory.getBean(ContextIdNames.CART_ORDER);

        extCartOrder.setShoppingCartGuid(shoppingCart.getGuid());

        CartOrderTermsAndConditionsFlag cartOrderTermsAndConditionsFlag;

        cartOrderTermsAndConditionsFlag = this.beanFactory.getBean("cartOrderTermsAndConditionsFlag");
        cartOrderTermsAndConditionsFlag.setCode(MYTRUETNCCODE);
        cartOrderTermsAndConditionsFlag.setAccepted(true);
        extCartOrder.addCartOrderTermsAndConditionsFlag(cartOrderTermsAndConditionsFlag);

        cartOrderTermsAndConditionsFlag = this.beanFactory.getBean("cartOrderTermsAndConditionsFlag");
        cartOrderTermsAndConditionsFlag.setCode(MYFALSETNCCODE);
        cartOrderTermsAndConditionsFlag.setAccepted(false);
        extCartOrder.addCartOrderTermsAndConditionsFlag(cartOrderTermsAndConditionsFlag);

        CartOrder newCartOrder = this.cartOrderService.saveOrUpdate(extCartOrder);

        newCartOrder = this.cartOrderService.findByShoppingCartGuid(shoppingCart.getGuid());

        extCartOrder = (ExtCartOrder) newCartOrder;

        Map<String, CartOrderTermsAndConditionsFlag> cartOrderTermsAndConditionsFlags;

        cartOrderTermsAndConditionsFlags = extCartOrder.getCartOrderTermsAndConditionsFlags();

        assertNotNull(cartOrderTermsAndConditionsFlags);

        assertEquals(2, cartOrderTermsAndConditionsFlags.size());

        cartOrderTermsAndConditionsFlag = cartOrderTermsAndConditionsFlags.get(MYTRUETNCCODE);
        assertNotNull(cartOrderTermsAndConditionsFlags);
        assertEquals(MYTRUETNCCODE, cartOrderTermsAndConditionsFlag.getCode());
        assertTrue(cartOrderTermsAndConditionsFlag.isAccepted());

        cartOrderTermsAndConditionsFlag = cartOrderTermsAndConditionsFlags.get(MYFALSETNCCODE);
        assertNotNull(cartOrderTermsAndConditionsFlags);
        assertEquals(MYFALSETNCCODE, cartOrderTermsAndConditionsFlag.getCode());
        assertFalse(cartOrderTermsAndConditionsFlag.isAccepted());
    }

    private ShoppingCart configureAndPersistCart() {
        ShopperService shopperService = beanFactory.getBean(ContextIdNames.SHOPPER_SERVICE);
        Shopper shopper = TestShopperFactoryForTestApplication.getInstance().createNewShopperWithMemento();
        StoreTestPersister storeTestPersister = getTac().getPersistersFactory().getStoreTestPersister();
        Customer customer = storeTestPersister.createDefaultCustomer(store);
        shopper.setCustomer(customer);
        shopper.setStoreCode(store.getCode());
        shopper = shopperService.save(shopper);

        final CustomerSession customerSession =
                TestCustomerSessionFactoryForTestApplication.getInstance().createNewCustomerSessionWithContext(shopper);
        customerSession.setCurrency(Currency.getInstance("USD"));
        final ShoppingCartImpl shoppingCart = beanFactory.getBean(ContextIdNames.SHOPPING_CART);
        shoppingCart.setShopper(shopper);
        shoppingCart.setStore(store);
        String cartGuid = Utils.uniqueCode("CART");
        shoppingCart.getShoppingCartMemento().setGuid(cartGuid);
        shoppingCart.setCustomerSession(customerSession);
        shopper.setCurrentShoppingCart(shoppingCart);
        return shoppingCartService.saveOrUpdate(shoppingCart);
    }

}
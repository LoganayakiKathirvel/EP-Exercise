/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.extensions.termsandconditions;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.extensions.domain.termsandconditions.TermsAndConditionFlag;
import com.elasticpath.test.integration.BasicSpringContextTest;

/**
 * Integration test cases for the TermsAndConditionsFlag.
 */
public class TermsAndConditionsFlagTest extends BasicSpringContextTest {

    @Autowired
    private BeanFactory coreBeanFactory;

    @Test
    public void testTermsAndConditionsFlagBean() {
        TermsAndConditionFlag termsAndConditionFlag;

        termsAndConditionFlag = this.coreBeanFactory.getBean("termsAndConditionsFlag");

        assertNotNull(termsAndConditionFlag);
    }

}
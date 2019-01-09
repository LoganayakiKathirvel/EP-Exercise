/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.extensions.termsandconditions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.List;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.extensions.domain.termsandconditions.TermsAndConditionsFlag;
import com.elasticpath.extensions.service.termsandconditions.TermsAndConditionsFlagService;
import com.elasticpath.test.integration.BasicSpringContextTest;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Integration test cases for the TermsAndConditionsFlag.
 */
public class TermsAndConditionsFlagServiceTest extends BasicSpringContextTest {

    private static final String TNC_CODE_MYNEWTNCCODE = "mynewtnccode";

    private static final String TNC_CODE_USAGE = "specificTerms";

    private static final int SIZE_1 = 1;

    private static final int SIZE_2 = 2;

    private static final int SIZE_3 = 3;

    @Autowired
    private BeanFactory coreBeanFactory;

    @Autowired
    private TermsAndConditionsFlagService termsAndConditionFlagService;

    @Test
    public void testFindAllTermsAndConditionFlags() {

        List<TermsAndConditionsFlag> termsAndConditionFlags;

        termsAndConditionFlags = termsAndConditionFlagService.findAllTermsAndConditionFlags();

        assertNotNull(termsAndConditionFlags);

        assertEquals(SIZE_2, termsAndConditionFlags.size());
    }

    @Test
    public void testFindTermsAndConditionFlagByCode() {
        TermsAndConditionsFlag termsAndConditionFlag;

        termsAndConditionFlag = termsAndConditionFlagService.findByCode(TNC_CODE_USAGE);

        assertNotNull(termsAndConditionFlag);

        assertEquals(TNC_CODE_USAGE, termsAndConditionFlag.getCode());
    }

    @Test
    @DirtiesDatabase
    public void testAddTermsAndConditionFlag() {
        TermsAndConditionsFlag termsAndConditionsFlag;

        termsAndConditionsFlag = coreBeanFactory.getBean("termsAndConditionsFlag");
        termsAndConditionsFlag.setCode(TNC_CODE_MYNEWTNCCODE);

        termsAndConditionFlagService.add(termsAndConditionsFlag);

        List<TermsAndConditionsFlag> termsAndConditionFlags = termsAndConditionFlagService.findAllTermsAndConditionFlags();

        assertEquals(SIZE_3, termsAndConditionFlags.size());

        termsAndConditionsFlag = termsAndConditionFlagService.findByCode(TNC_CODE_MYNEWTNCCODE);

        assertNotNull(termsAndConditionsFlag);

        assertEquals(TNC_CODE_MYNEWTNCCODE, termsAndConditionsFlag.getCode());
    }

    @Test
    @DirtiesDatabase
    public void testDeleteTermsAndConditionFlag() {
        TermsAndConditionsFlag termsAndConditionsFlag;

        termsAndConditionsFlag = termsAndConditionFlagService.findByCode(TNC_CODE_USAGE);

        termsAndConditionFlagService.delete(termsAndConditionsFlag);

        List<TermsAndConditionsFlag> termsAndConditionFlags = termsAndConditionFlagService.findAllTermsAndConditionFlags();

        assertEquals(SIZE_1, termsAndConditionFlags.size());
    }
}
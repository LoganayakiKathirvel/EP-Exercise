/**
 * Copyright (c) Elastic Path Software Inc., 2017
 */
package com.elasticpath.extensions.termsandconditions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;

import com.elasticpath.commons.beanframework.BeanFactory;
import com.elasticpath.extensions.domain.termsandconditions.TermsAndConditionFlag;
import com.elasticpath.extensions.domain.termsandconditions.impl.TermsAndConditionsFlagImpl;
import com.elasticpath.persistence.api.PersistenceEngine;
import com.elasticpath.test.db.DbTestCase;
import com.elasticpath.test.integration.DirtiesDatabase;

/**
 * Integration test cases for the TermsAndConditionsFlag.
 */
public class TermsAndConditionsFlagPersistenceTest extends DbTestCase {

    private static final long VALUE_UIDPK = 100L;

    @Autowired
    private BeanFactory coreBeanFactory;

    @Autowired
    private PersistenceEngine persistenceEngine;

    @Test
    public void testGetTermsAndConditionFlag() {
        TermsAndConditionFlag termsAndConditionFlag;

        long uidpk = VALUE_UIDPK;

        termsAndConditionFlag = persistenceEngine.get(TermsAndConditionsFlagImpl.class, uidpk);

        assertNotNull(termsAndConditionFlag);

        assertEquals("generalTerms", termsAndConditionFlag.getCode());
    }

    @Test
    @DirtiesDatabase
    public void testAddTermsAndConditionFlag() {
        TermsAndConditionFlag termsAndConditionFlag;

        termsAndConditionFlag = this.coreBeanFactory.getBean("termsAndConditionsFlag");

        termsAndConditionFlag.setCode("newtnccode");

        TermsAndConditionFlag newTermsAndConditionFlag;

        newTermsAndConditionFlag = persist(termsAndConditionFlag);

        long uidpk = newTermsAndConditionFlag.getUidPk();

        termsAndConditionFlag = persistenceEngine.get(TermsAndConditionsFlagImpl.class, uidpk);

        assertNotNull(termsAndConditionFlag);

        assertEquals("newtnccode", termsAndConditionFlag.getCode());
    }

    private TermsAndConditionFlag persist(final TermsAndConditionFlag termsAndConditionFlag) {
        // We don't cascade persist onto product types, so make sure it exists.
        return getTxTemplate().execute(new TransactionCallback<TermsAndConditionFlag>() {
            @Override
            public TermsAndConditionFlag doInTransaction(final TransactionStatus arg0) {
                return getPersistenceEngine().saveOrUpdate(termsAndConditionFlag);
            }
        });
    }
}
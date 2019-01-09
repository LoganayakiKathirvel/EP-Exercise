
/**
 * Copyright (c) Elastic Path Software Inc., 2007
 */

package com.elasticpath.extensions.service.termsandconditions.impl;

import java.util.List;

import com.elasticpath.extensions.domain.termsandconditions.TermsAndConditionsFlag;
import com.elasticpath.extensions.service.termsandconditions.TermsAndConditionsFlagService;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.api.PersistenceEngine;

/**
 * Service for managing TermsAndConditionFlags.
 */
public class TermsAndConditionsFlagServiceImpl implements TermsAndConditionsFlagService {
    private PersistenceEngine persistenceEngine;

    @Override
    public TermsAndConditionsFlag add(final TermsAndConditionsFlag termsAndConditionFlag) {
        TermsAndConditionsFlag newTermsAndConditionFlag;

        try {
            newTermsAndConditionFlag = this.persistenceEngine.saveOrUpdate(termsAndConditionFlag);
        } catch (final Exception ex) {
            throw new EpPersistenceException("Exception on adding TermsAndConditionFlag.", ex);
        }

        return newTermsAndConditionFlag;
    }

    @Override
    public void delete(final TermsAndConditionsFlag termsAndConditionFlag) {
        getPersistenceEngine().delete(termsAndConditionFlag);
        getPersistenceEngine().flush();
    }

    @Override
    public TermsAndConditionsFlag findByCode(final String code) {
        final List<TermsAndConditionsFlag> termsAndConditionFlags =
                this.persistenceEngine.retrieveByNamedQuery("FIND_TNC_BY_CODE", code);

        if (!termsAndConditionFlags.isEmpty()) {
            return termsAndConditionFlags.get(0);
        }
        return null;
    }

    @Override
    public List<TermsAndConditionsFlag> findAllTermsAndConditionFlags() {
        return this.getPersistenceEngine().retrieveByNamedQuery("FIND_ALL_TNCS");
    }

    /**
     * Gets the PersistenceEngine.
     *
     * @return the PersistenceEngine
     */
    public PersistenceEngine getPersistenceEngine() {
        return persistenceEngine;
    }

    /**
     * Sets the PersistenceEngine.
     *
     * @param persistenceEngine the PersistenceEngine
     */
    public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
        this.persistenceEngine = persistenceEngine;
    }

}

package com.elasticpath.extensions.service.termsandconditions.impl;

import java.util.List;

import com.elasticpath.extensions.domain.termsandconditions.TermsAndConditionFlag;
import com.elasticpath.extensions.service.termsandconditions.TermsAndConditionsFlagService;
import com.elasticpath.persistence.api.EpPersistenceException;
import com.elasticpath.persistence.api.PersistenceEngine;

/**
 * Implementation of TnC service methods.
 */
public class TermsAndConditionsFlagServiceImpl implements TermsAndConditionsFlagService {
    private PersistenceEngine persistenceEngine;


    @Override
    public TermsAndConditionFlag add(final TermsAndConditionFlag termsAndConditionFlag) {
        TermsAndConditionFlag newTermsAndConditionFlag;

        try {
            newTermsAndConditionFlag = this.persistenceEngine.saveOrUpdate(termsAndConditionFlag);
        } catch (final Exception ex) {
            throw new EpPersistenceException("Exception on adding TermsAndConditionFlag.", ex);
        }

        return newTermsAndConditionFlag;
    }

    @Override
    public void delete(final TermsAndConditionFlag termsAndConditionFlag) {
        getPersistenceEngine().delete(termsAndConditionFlag);
        getPersistenceEngine().flush();
    }

    @Override
    public TermsAndConditionFlag findByCode(final String code) {
        final List<TermsAndConditionFlag> termsAndConditionFlags =
                this.persistenceEngine.retrieveByNamedQuery("FIND_TNC_BY_CODE", code);

        if (!termsAndConditionFlags.isEmpty()) {
            return termsAndConditionFlags.get(0);
        }
        return null;
    }

    @Override
    public List<TermsAndConditionFlag> findAllTermsAndConditionFlags() {
        return this.getPersistenceEngine().retrieveByNamedQuery("FIND_ALL_TNCS");
    }

    /**
     * @return PersistenceEngine
     */
    public PersistenceEngine getPersistenceEngine() {
        return persistenceEngine;
    }

    /**
     * @param persistenceEngine PersistenceEngine
     */
    public void setPersistenceEngine(final PersistenceEngine persistenceEngine) {
        this.persistenceEngine = persistenceEngine;
    }

}
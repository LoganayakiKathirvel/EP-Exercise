package com.elasticpath.extensions.service.termsandconditions;

import java.util.List;
import com.elasticpath.extensions.domain.termsandconditions.TermsAndConditionsFlag;

/**
 * Service for managing TermsAndConditionFlags.
 */
public interface TermsAndConditionsFlagService {

    /**
     * Adds a new TermsAndConditionFlag.
     *
     * @param termsAndConditionFlag the TermsAndConditionFlag to add
     * @return the new TermsAndConditionFlag
     */
    TermsAndConditionsFlag add(TermsAndConditionsFlag termsAndConditionFlag);

    /**
     * Deletes a TermsAndConditionFlag.
     *
     * @param termsAndConditionFlag the TermsAndConditionFlag to delete
     */
    void delete(TermsAndConditionsFlag termsAndConditionFlag);

    /**
     * Finds a TermsAndConditionFlag by its code.
     *
     * @param code the code
     * @return the TermsAndConditionFlag or null
     */
    TermsAndConditionsFlag findByCode(String code);

    /**
     * Finds all TermsAndConditionFlags.
     *
     * @return list of TermsAndConditionFlags
     */
    List<TermsAndConditionsFlag> findAllTermsAndConditionFlags();

}

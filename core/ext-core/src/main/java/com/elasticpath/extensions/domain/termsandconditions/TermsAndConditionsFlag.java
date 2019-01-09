package com.elasticpath.extensions.domain.termsandconditions;

import com.elasticpath.persistence.api.Entity;

/**
 * Base interface for Terms and Conditions codes.
 */
public interface TermsAndConditionsFlag extends Entity {

    /**
     * Gets the TnC code.
     *
     * @return the code.
     */
    String getCode();

    /**
     * Sets the TnC code.
     *
     * @param code the code
     */
    void setCode(String code);

}
package com.elasticpath.extensions.domain.termsandconditions;

import com.elasticpath.persistence.api.Persistable;

/**
 * Interace for the CartOrder TermsAndConditionsFlag.
 */
public interface CartOrderTermsAndConditionsFlag extends Persistable {

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

    /**
     * Returns whether the TnC is accepted.
     *
     * @return true if accepted, false otherwise
     */
    boolean isAccepted();

    /**
     * Set the accepted flag.
     *
     * @param bAccepted true if accepted, false otherwise
     */
    void setAccepted(boolean bAccepted);

}

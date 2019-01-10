/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.extensions.orders;

import com.elasticpath.cmclient.core.nls.LocalizedMessagePostProcessor;

/**
 * Ext Order localization class.
 */
public final class ExtOrderMessages {

    private static final String BUNDLE_NAME = "com.elasticpath.extensions.orders.ExtOrderMessages"; //$NON-NLS-1$

    private ExtOrderMessages() {
    }

    /**
     * Localized String Keys.
     */

    public String TermsAndConditionsLabel;

    public String TermsAndConditionSectionTitle;



    /**
     * Gets the NLS localize message class.
     * @return the localized message class.
     */
    public static ExtOrderMessages get() {
        return LocalizedMessagePostProcessor.getUTF8Encoded(BUNDLE_NAME, ExtOrderMessages.class);
    }

}
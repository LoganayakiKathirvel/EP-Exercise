/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.extensions.domain.termsandconditions;

import com.elasticpath.persistence.api.Persistable;

/**
 * Interface CartOrderTermsAndConditionsFlag.
 */
public interface CartOrderTermsAndConditionsFlag extends Persistable {
	/**
	 * @return code
	 */
	String getCode();

	/**
	 * @param code code
	 */
	void setCode(String code);

	/**
	 * @return boolean value
	 */
	boolean isAccepted();

	/**
	 * @param accepted accepted
	 */
	void setAccepted(boolean accepted);
}

/*
 * Copyright © 2018 Elastic Path Software Inc. All rights reserved.
 */

package com.elasticpath.extensions.domain.termsandconditions.impl;

import javax.persistence.Basic;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.apache.openjpa.persistence.DataCache;

import com.elasticpath.extensions.domain.termsandconditions.CartOrderTermsAndConditionsFlag;
import com.elasticpath.persistence.api.AbstractPersistableImpl;

/**
 * CartOrderTermsAndConditionsFlagImpl by extending AbstractPersistableImpl.
 */
@Entity
@Table(name = CartOrderTermsAndConditionsFlagImpl.TABLE_NAME)
@DataCache(enabled = false)
public final class CartOrderTermsAndConditionsFlagImpl extends AbstractPersistableImpl implements CartOrderTermsAndConditionsFlag {
	private static final long serialVersionUID = 5000000001L;

	/**
	 * The name of the DB table used to persist this object.
	 */
	static final String TABLE_NAME = "TCARTORDERTNCS";

	private long uidPk;
	private String code;
	private boolean accepted;

	@Basic
	@Column(name = "CODE")
	@Override
	public String getCode() {
		return code;
	}

	/**
	 * @param code code
	 */
	@Override
	public void setCode(final String code) {
		this.code = code;
	}

	@Basic
	@Column(name = "ACCEPTED")
	@Override
	public boolean isAccepted() {
		return accepted;
	}

	/**
	 * @param accepted accepted
	 */
	@Override
	public void setAccepted(final boolean accepted) {
		this.accepted = accepted;
	}

	/**
	 * Gets the unique identifier for this domain object. This unique identifier is system-dependent. That means on different systems(like staging
	 * and production environments), different identifiers might be assigned to the same(from business perspective) domain object.
	 * <p>
	 * Notice: not all persistent domain objects has unique identifier. Some value objects don't have unique identifier. They are cascade loaded and
	 * updated through their parents.
	 *
	 * @return the unique identifier.
	 */
	@Id
	@Column(name = "UIDPK")
	@GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
	@TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue =
			TABLE_NAME)
	@Override
	public long getUidPk() {
		return uidPk;
	}

	/**
	 * Sets the unique identifier for this domain model object.
	 *
	 * @param uidPk the new unique identifier.
	 */
	@Override
	public void setUidPk(final long uidPk) {
		this.uidPk = uidPk;
	}
}

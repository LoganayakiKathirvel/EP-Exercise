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

import com.elasticpath.extensions.domain.termsandconditions.TermsAndConditionsFlag;
import com.elasticpath.persistence.api.AbstractEntityImpl;

/**
 * Base implementation for Terms and Conditions codes.
 */
@Entity
@Table(name = TermsAndConditionsFlagImpl.TABLE_NAME)
@DataCache(enabled = true)
public class TermsAndConditionsFlagImpl extends AbstractEntityImpl
        implements TermsAndConditionsFlag {

    /** Serial version id. */
    private static final long serialVersionUID = 5000000001L;

    /** Database Table. */
    public static final String TABLE_NAME = "TTERMSANDCONDITIONS";

    private String code;
    private String guid;
    private long uidpk;

    @Basic
    @Column(name = "CODE")
    @Override
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(final String code) {
        this.code = code;
    }

    @Override
    @Basic
    @Column(name = "GUID")
    public String getGuid() {
        return guid;
    }

    @Override
    public void setGuid(final String guid) {
        this.guid = guid;
    }

    @Override
    @Id
    @Column(name = "UIDPK")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
    @TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
    public long getUidPk() {
        return uidpk;
    }

    @Override
    public void setUidPk(final long uidpk) {
        this.uidpk = uidpk;
    }

}
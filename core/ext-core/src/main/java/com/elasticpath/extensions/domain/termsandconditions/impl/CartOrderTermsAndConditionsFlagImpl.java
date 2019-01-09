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
 * Implementation of CartOrderTermsAndConditionsFlag.
 */
@Entity
@Table(name = CartOrderTermsAndConditionsFlagImpl.TABLE_NAME)
@DataCache(enabled = false)
public class CartOrderTermsAndConditionsFlagImpl extends AbstractPersistableImpl implements CartOrderTermsAndConditionsFlag {
    private static final long serialVersionUID = 5000000001L;

    /** The name of the DB table used to persist this object. */
    static final String TABLE_NAME = "TCARTORDERTNCS";

    private long uidPk;
    private String code;
    private boolean accepted;


    @Override
    @Id
    @Column(name = "UIDPK")
    @GeneratedValue(strategy = GenerationType.TABLE, generator = TABLE_NAME)
    @TableGenerator(name = TABLE_NAME, table = "JPA_GENERATED_KEYS", pkColumnName = "ID", valueColumnName = "LAST_VALUE", pkColumnValue = TABLE_NAME)
    public long getUidPk() {
        return uidPk;
    }

    @Override
    public void setUidPk(final long uidPk) {
        this.uidPk = uidPk;
    }

    @Override
    @Basic
    @Column(name = "CODE")
    public String getCode() {
        return code;
    }

    @Override
    public void setCode(final String code) {
        this.code = code;
    }

    @Override
    @Basic
    @Column(name = "ACCEPTED")
    public boolean isAccepted() {
        return accepted;
    }

    @Override
    public void setAccepted(final boolean accepted) {
        this.accepted = accepted;
    }

}

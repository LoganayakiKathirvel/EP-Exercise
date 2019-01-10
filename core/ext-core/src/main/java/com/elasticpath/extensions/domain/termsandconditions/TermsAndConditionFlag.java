package com.elasticpath.extensions.domain.termsandconditions;

import com.elasticpath.persistence.api.Entity;

/*Interface for termscondition flag **/
public interface TermsAndConditionFlag extends Entity { /* return code */ String getCode(); /* @param code setting */ void setCode(String code);
}

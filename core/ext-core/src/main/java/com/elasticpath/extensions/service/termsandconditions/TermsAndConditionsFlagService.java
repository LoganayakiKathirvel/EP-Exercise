package com.elasticpath.extensions.service.termsandconditions;
import java.util.List;
import com.elasticpath.extensions.domain.termsandconditions.TermsAndConditionFlag;

public interface TermsAndConditionsFlagService {
    /**
     * @param termsAndConditionFlag termsAndConditionFlag
     * @return TermsAndConditionsFlag
     */
    TermsAndConditionFlag add(TermsAndConditionFlag termsAndConditionFlag);

    /**
     * @param termsAndConditionFlag TermsAndConditionsFlag
     */
    void delete(TermsAndConditionFlag termsAndConditionFlag);

    /**
     * @param code code
     * @return TermsAndConditionsFlag
     */
    TermsAndConditionFlag findByCode(String code);

    /**
     * @return list of TermsAndConditionsFlag
     */
    List<TermsAndConditionFlag> findAllTermsAndConditionFlags();
}

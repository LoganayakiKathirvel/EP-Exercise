/*
 * Copyright © 2017 Elastic Path Software Inc. All rights reserved.
 */
package com.elasticpath.extensions.orders;


import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import com.elasticpath.cmclient.core.editors.AbstractCmClientEditorPageSectionPart;
import com.elasticpath.cmclient.core.editors.AbstractCmClientFormEditor;
import com.elasticpath.cmclient.core.helpers.extenders.EpSectionCreator;
import com.elasticpath.cmclient.core.ui.framework.AbstractCmClientFormSectionPart;
import com.elasticpath.domain.order.Order;

import org.eclipse.swt.widgets.Text;

/**
 * Extension order Section.
 */
public class ExtOrderSection extends AbstractCmClientEditorPageSectionPart {
    private Text controlValue;

    /**
     * Constructor.
     * @param formPage the form page.
     * @param editor the editor.
     * @param style the style.
     */
    public ExtOrderSection(final FormPage formPage, final AbstractCmClientFormEditor editor, final int style) {
        super(formPage, editor, style);
    }

    @Override
    protected void createControls(final Composite composite, final FormToolkit formToolkit) {
        formToolkit.createLabel(composite,  ExtOrderMessages.get().TermsAndConditionsLabel);
        controlValue = formToolkit.createText(composite, "");
    }

    @Override
    protected void populateControls() {
        Order order = (Order) getModel();
        String generalTermsAndConditionsCode = "generalTerms";
        String termsAndConditionsCode = order.getFieldValue(generalTermsAndConditionsCode);
        if (StringUtils.isEmpty(termsAndConditionsCode)) {
            termsAndConditionsCode = "";
        }
        controlValue.setText(termsAndConditionsCode);
    }

    @Override
    protected void bindControls(final DataBindingContext dataBindingContext) {
        // Empty by default.
    }

    @Override
    protected String getSectionTitle() {
        return  ExtOrderMessages.get().TermsAndConditionSectionTitle;
    }

    /**
     * Creator class, required because we need a no-arg constructor.
     */
    public static class Creator implements EpSectionCreator {

        /**
         * Instantiation method for the section.
         * @param formPage the form page.
         * @param abstractCmClientFormEditor the abstractCmClientFormEditor.
         * @return AbstractCmClientFormSectionPart.
         */
        public AbstractCmClientFormSectionPart instantiateSection(final FormPage formPage,
                                                                  final AbstractCmClientFormEditor abstractCmClientFormEditor) {
            return new ExtOrderSection(formPage, abstractCmClientFormEditor, ExpandableComposite.TITLE_BAR | ExpandableComposite.EXPANDED);
        }
    }
}
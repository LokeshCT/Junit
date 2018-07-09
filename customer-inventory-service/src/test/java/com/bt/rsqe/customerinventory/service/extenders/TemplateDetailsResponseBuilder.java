package com.bt.rsqe.customerinventory.service.extenders;

import com.bt.rsqe.tpe.multisite.Common_Mandatory;
import com.bt.rsqe.tpe.multisite.Row_Mandatory;
import com.bt.rsqe.tpe.multisite.TPE_TemplateDetails_Response;
import com.bt.rsqe.tpe.multisite.Template_Mandatory_Attributes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TemplateDetailsResponseBuilder {
    Map<Integer, Map<Integer, Map<Integer, Template_Mandatory_Attributes>>> commonAttributes = new HashMap<Integer, Map<Integer, Map<Integer, Template_Mandatory_Attributes>>>();
    private List<Template_Mandatory_Attributes> primaryMandatoryAttributes = new ArrayList<Template_Mandatory_Attributes>();

    public TPE_TemplateDetails_Response build() {
        final TPE_TemplateDetails_Response tpe_templateDetails_response = new TPE_TemplateDetails_Response();

        if(commonAttributes.size()>0) {
            Common_Mandatory[] commonMandatories = new Common_Mandatory[Collections.max(commonAttributes.keySet()) + 1];
            for (Integer mandatoryGroup : commonAttributes.keySet()) {
                if (commonMandatories[mandatoryGroup] == null) {
                    commonMandatories[mandatoryGroup] = new Common_Mandatory();
                }

                final Map<Integer, Map<Integer, Template_Mandatory_Attributes>> rows = commonAttributes.get(mandatoryGroup);
                if (commonMandatories[mandatoryGroup].getRow() == null) {
                    Row_Mandatory[] mandatoryRows = new Row_Mandatory[Collections.max(rows.keySet()) + 1];
                    commonMandatories[mandatoryGroup].setRow(mandatoryRows);
                }

                for (Integer rowIndex : rows.keySet()) {
                    if (commonMandatories[mandatoryGroup].getRow()[rowIndex] == null) {
                        Row_Mandatory newRow = new Row_Mandatory();
                        commonMandatories[mandatoryGroup].getRow()[rowIndex] = newRow;
                    }

                    final Map<Integer, Template_Mandatory_Attributes> attributes = rows.get(rowIndex);
                    if (commonMandatories[mandatoryGroup].getRow()[rowIndex].getMandate_Info() == null) {
                        Template_Mandatory_Attributes[] mandatoryAttributes = new Template_Mandatory_Attributes[Collections.max(attributes.keySet()) + 1];
                        commonMandatories[mandatoryGroup].getRow()[rowIndex].setMandate_Info(mandatoryAttributes);
                    }

                    for (Integer attributeIndex : attributes.keySet()) {
                        commonMandatories[mandatoryGroup].getRow()[rowIndex].getMandate_Info()[attributeIndex] = attributes.get(attributeIndex);
                    }
                }
            }
            tpe_templateDetails_response.setCommon_Info(commonMandatories);
        }

        if (primaryMandatoryAttributes.size() > 0) {
            final Template_Mandatory_Attributes[] primaryMandatories = primaryMandatoryAttributes.toArray(new Template_Mandatory_Attributes[primaryMandatoryAttributes.size()]);
            tpe_templateDetails_response.setPrimary(primaryMandatories);
        }

        return tpe_templateDetails_response;
    }

    public TemplateDetailsResponseBuilder withCommonMandatoryAttribute(int mandatoryGroup, int rowIndex, int attributeIndex, Template_Mandatory_Attributes template_mandatory_attributes) {
        if(!commonAttributes.containsKey(mandatoryGroup)){
            commonAttributes.put(mandatoryGroup, new HashMap<Integer, Map<Integer, Template_Mandatory_Attributes>>());
        }
        final Map<Integer, Map<Integer, Template_Mandatory_Attributes>> rows = commonAttributes.get(mandatoryGroup);
        if(!rows.containsKey(rowIndex)){
            rows.put(rowIndex, new HashMap<Integer, Template_Mandatory_Attributes>());
        }
        final Map<Integer, Template_Mandatory_Attributes> attributes = rows.get(rowIndex);
        if(!attributes.containsKey(attributeIndex)){
            attributes.put(attributeIndex, template_mandatory_attributes);
        }
        return this;
    }

    public TemplateDetailsResponseBuilder withCommonMandatoryAttribute(int mandatoryGroup, int rowIndex, int attributeIndex) {
        withCommonMandatoryAttribute(mandatoryGroup, rowIndex, attributeIndex, new Template_Mandatory_Attributes());
        return this;
    }

    public TemplateDetailsResponseBuilder withPrimaryMandatoryAttribute(){
        primaryMandatoryAttributes.add(new Template_Mandatory_Attributes());
        return this;
    }

    public TemplateDetailsResponseBuilder withPrimaryMandatoryAttribute(Template_Mandatory_Attributes attributeName) {
        primaryMandatoryAttributes.add(attributeName);
        return this;
    }
}

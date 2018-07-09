package com.bt.rsqe.ape;

import com.bt.rsqe.ape.config.ApeMappingConfigLoader;
import com.bt.rsqe.ape.config.LocalIdentifier;
import com.bt.rsqe.ape.config.OfferingAttribute;
import com.bt.rsqe.ape.dto.ApeQref;
import com.bt.rsqe.ape.dto.ApeQrefAttributeDetail;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static org.apache.commons.lang.StringUtils.isNotEmpty;

public class QrefAttributeExtractor {
    private ApeQref apeQref;

    public QrefAttributeExtractor(ApeQref apeQref) {
        this.apeQref = apeQref;
    }

    public String getAttributeValue(LocalIdentifier localIdentifier) {
        List<String> qrefAttributeNames = getQrefAttributeNames(localIdentifier);

        for(ApeQrefAttributeDetail attributeDetail : apeQref.getAttributes()) {
            if(qrefAttributeNames.contains(attributeDetail.getAttributeName())) {
                return attributeDetail.getAttributeValue();
            }
        }

        return null;
    }

    public Double getAttributeDoubleValue(LocalIdentifier localIdentifier) {
        String attributeValue = getAttributeValue(localIdentifier);
        if( isNotEmpty(attributeValue)) {
            return Double.parseDouble(attributeValue);
        }
        return null;
    }

    public Date getAttributeDateValue(LocalIdentifier localIdentifier) {
        String attributeValue = getAttributeValue(localIdentifier);
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy/MM/dd");
        if (isNotEmpty(attributeValue)) {
            try {
                return formatter.parse(attributeValue);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private List<String> getQrefAttributeNames(LocalIdentifier localIdentifier) {
        OfferingAttribute[] offeringAttributes = ApeMappingConfigLoader.getLocalIdentifierMappings()
                                                                       .getLocalIdentifierMappingConfig(localIdentifier.name())
                                                                       .getOfferingAttributeConfig();

        return newArrayList(Iterables.transform(newArrayList(offeringAttributes), new Function<OfferingAttribute, String>() {
            @Override
            public String apply(@Nullable OfferingAttribute offeringAttribute) {
                return offeringAttribute.getName();
            }
        }));
    }
}

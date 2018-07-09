package com.bt.rsqe.customerinventory.service.evaluators;

import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import org.apache.commons.lang.StringUtils;

public class SubscriberId {
    public static final int SUBSCRIBER_ID_LENGTH = 11;
    private static SubscriberIdLogger logger = LogFactory.createDefaultLogger(SubscriberIdLogger.class);

    public static String fromAssetUniqueId(String assetUniqueId) {
        int assetUniqueIdLen = assetUniqueId.length();

        if(assetUniqueIdLen > SUBSCRIBER_ID_LENGTH) {
            logger.warnSubscriberIdUniqueness(assetUniqueIdLen);
        }

        if (assetUniqueIdLen > SUBSCRIBER_ID_LENGTH) {
            return assetUniqueId.substring(0, SUBSCRIBER_ID_LENGTH);
        }else {
            return StringUtils.leftPad(assetUniqueId, SUBSCRIBER_ID_LENGTH, "0");
        }
    }

    interface SubscriberIdLogger {
        @Log(level = LogLevel.WARN, format = "**********Asset Unique Id has reached the length [%s], so the uniqueness of subscriberId may be broken***********")
        void warnSubscriberIdUniqueness(int assetUniqueIdLen);
    }
}

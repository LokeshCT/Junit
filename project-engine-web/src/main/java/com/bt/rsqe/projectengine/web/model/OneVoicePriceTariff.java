package com.bt.rsqe.projectengine.web.model;

import com.bt.rsqe.enums.PriceType;

import java.util.Set;

import static com.bt.rsqe.enums.PriceType.*;
import static com.google.common.collect.Sets.*;

public enum OneVoicePriceTariff {
    VPN_CONFIG(13449L, ONE_TIME),
    VPN_SUBSCRIPTION(13450L, RECURRING),
    DIAL_PLAN_CHANGE_CONFIG(13451L, ONE_TIME),
    MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG(13452L, ONE_TIME),
    AMENDMENT_CHARGE(13453L, ONE_TIME),
    CANCELLATION_CHARGE(13454L, ONE_TIME),

    GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG(13445L, ONE_TIME),
    GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION(13446L, RECURRING),
    GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG(13447L, ONE_TIME),
    GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION(13448L, RECURRING),
    GLOBAL_DIRECT_LITE_CHANNEL_CONFIG(13442L, ONE_TIME),
    GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION(13443L, RECURRING);

    public static Set<OneVoicePriceTariff> NON_CHANNEL_BASED_PPSR_IDS =
        newHashSet(VPN_CONFIG,
                   VPN_SUBSCRIPTION,
                   DIAL_PLAN_CHANGE_CONFIG,
                   MAJOR_MOVE_ADDS_OR_CHANGE_CONFIG,
                   AMENDMENT_CHARGE,
                   CANCELLATION_CHARGE);

    public static final Set<OneVoicePriceTariff> CHANNEL_BASED_PPSR_IDS =
        newHashSet(GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG,
                   GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION,
                   GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG,
                   GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION,
                   GLOBAL_DIRECT_LITE_CHANNEL_CONFIG,
                   GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION);

    private final Long ppsrId;
    private PriceType type;

    OneVoicePriceTariff(Long ppsrId, PriceType type) {
        this.ppsrId = ppsrId;
        this.type = type;
    }

    public Long ppsrId() {
        return ppsrId;
    }

    public PriceType priceType() {
        return type;
    }

    public boolean isForType(PriceType priceType) {
        return type == priceType;
    }

    public static OneVoicePriceTariff forId(Long ppsrId) {
        for (OneVoicePriceTariff candidate : values()) {
            if (candidate.ppsrId.equals(ppsrId)) {
                return candidate;
            }
        }
        throw new IllegalArgumentException(String.format("Unknown ppsr id: %s", ppsrId));
    }
}
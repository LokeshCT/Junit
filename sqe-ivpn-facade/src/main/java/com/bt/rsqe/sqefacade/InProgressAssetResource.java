package com.bt.rsqe.sqefacade;

import com.bt.rsqe.asset.ivpn.IVPNQuote;
import com.bt.rsqe.asset.ivpn.dto.IVPNConfigurationDTO;
import com.bt.rsqe.domain.project.SiteId;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.bt.rsqe.sqefacade.domain.IvpnAssetId;
import com.bt.rsqe.web.rest.RestRequestBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Maps.*;
import static org.apache.commons.lang.StringUtils.*;

public class InProgressAssetResource {
    private static final InProgressAssetResourceLogger LOG = LogFactory.createDefaultLogger(InProgressAssetResourceLogger.class);
    public static final String SITE_ID_PARAM = "siteId";
    public static final String QUOTE_OPTION_NAME_PARAM = "quoteOptionId";

    private final RestRequestBuilder restRequestBuilder;

    public InProgressAssetResource(RestRequestBuilder restRequestBuilder) {
        this.restRequestBuilder = restRequestBuilder;
    }

    public InProgressAssetResource(SqeIvpnFacadeConfig config) {
        this(new RestRequestBuilder(URI.create(config.getServiceEndPointConfig(SqeIvpnFacadeConfig.INPROGRESS_ASSETS).getUri())));
    }

    public List<IVPNConfigurationDTO> get(SiteId siteId, String quoteName) {
        if( isValidSiteId(siteId) || isNotBlank(quoteName))  {
            try {
                IVPNQuote iVPNQuote = restRequestBuilder.build(
                                                        withQueryParams(siteId.toString(), quoteName)
                                                    ).get().getEntity(IVPNQuote.class);

                return iVPNQuote.toDTO();
            } catch (Exception e) {
                LOG.error(siteId, quoteName, e.getMessage());
            }
        }
        return newArrayList();
    }

    public List<IVPNConfigurationDTO> get(String quoteName) {

        try {
            IVPNQuote iVPNQuote = restRequestBuilder.build(
                                                        withQueryParam(quoteName)
                                                          ).get().getEntity(IVPNQuote.class);
            return iVPNQuote.toDTO();
        } catch (Exception e) {
            LOG.error(quoteName, e.getMessage());
        }
        return newArrayList();
    }

    public IVPNConfigurationDTO get(SiteId siteId, IvpnAssetId uuid) {
            return getOnlyElement(restRequestBuilder.build("sites", siteId.toString(), "assets", uuid.getValue())
                                     .get().getEntity(IVPNQuote.class).toDTO());
    }

    private Map<String, String> withQueryParams(String siteId, String quoteName) {
        Map<String, String> queryParams = newHashMap();
        queryParams.put(SITE_ID_PARAM, siteId);
        queryParams.put(QUOTE_OPTION_NAME_PARAM, quoteName);
        return queryParams;
    }

    private Map<String, String> withQueryParam(String quoteName) {
        Map<String, String> queryParams = newHashMap();
        queryParams.put(QUOTE_OPTION_NAME_PARAM, quoteName);
        return queryParams;
    }

    private boolean isValidSiteId(SiteId siteId) {
        return siteId != null && isNotBlank(siteId.toString());
    }

    interface InProgressAssetResourceLogger {
        @Log(level = LogLevel.ERROR, loggerName = "InprogressAssetResourceLogger", format = "Error while fetching in-progress assets for site %s and quoteOption %s - %s")
        void error(SiteId siteId, String quoteOptionName, String message);

        @Log(level = LogLevel.ERROR, loggerName = "InprogressAssetResourceLogger", format = "Error while fetching in-progress assets for quoteOption %s - %s")
        void error(String quoteOptionName, String message);
    }
}

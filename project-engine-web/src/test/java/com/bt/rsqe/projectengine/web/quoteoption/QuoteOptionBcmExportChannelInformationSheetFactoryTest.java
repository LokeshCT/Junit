package com.bt.rsqe.projectengine.web.quoteoption;

import com.bt.rsqe.customerinventory.dto.FutureAssetPricesDTO;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.enums.PriceType;
import com.bt.rsqe.enums.ProductCodes;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.bt.rsqe.projectengine.web.facades.FlattenedProductStructure;
import com.bt.rsqe.projectengine.web.facades.FutureProductInstanceFacade;
import com.bt.rsqe.projectengine.web.facades.ProductIdentifierFacade;
import com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture;
import com.bt.rsqe.projectengine.web.model.FutureAssetPricesModel;
import com.bt.rsqe.projectengine.web.model.LineItemModel;
import com.bt.rsqe.projectengine.web.model.OneVoiceConfiguration;
import com.bt.rsqe.projectengine.web.model.OneVoicePriceTariff;
import com.bt.rsqe.security.UserContextManager;
import com.bt.rsqe.utils.RSQEMockery;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.jmock.integration.junit4.JMock;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

import static com.bt.rsqe.expedio.fixtures.SiteDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesDTOFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.FutureAssetPricesModelFixture.*;
import static com.bt.rsqe.projectengine.web.fixtures.PriceLineDTOFixture.*;
import static com.bt.rsqe.security.UserContextBuilder.*;
import static com.google.common.collect.Lists.*;
import static org.hamcrest.MatcherAssert.*;
import static org.hamcrest.core.Is.*;
import static org.mockito.Mockito.*;

@RunWith(JMock.class)
public class QuoteOptionBcmExportChannelInformationSheetFactoryTest {
    private final Mockery context = new RSQEMockery();
    private QuoteOptionBcmExportChannelInformationSheetFactory factory;
    private FutureProductInstanceFacade mockFutureProductInstanceFacade = context.mock(FutureProductInstanceFacade.class);
    private SiteDTO site;
    private ProductIdentifierFacade productIdentifierFacade;
    private PricingConfig pricingConfig;

    @Before
    public void before() {
        site = aSiteDTO()
            .withBfgSiteId("siteId1")
            .withName("siteName1")
            .withFloor("floor1")
            .withBuilding("building1")
            .withCity("city1")
            .withCountry("country1")
            .withPostCode("postcode1")
            .build();
        factory = new QuoteOptionBcmExportChannelInformationSheetFactory(mockFutureProductInstanceFacade);
        withDirectUser();
        productIdentifierFacade = mock(ProductIdentifierFacade.class);
        pricingConfig = new PricingConfig();

    }

    @Test
    public void shouldGenerateSiteInformation() throws Exception {
        final FutureAssetPricesModel futureAssetPricesModel =
            aFutureAssetPricesModel()
                .with(aFutureAssetPricesDTO().build())
                .with(productIdentifierFacade)
                .build();
        final LineItemModel lineItem = context.mock(LineItemModel.class);
        context.checking(new Expectations() {{
            allowing(lineItem).getSite();
            will(returnValue(site));
            allowing(lineItem).getFutureAssetPricesModel();
            will(returnValue(futureAssetPricesModel));
            ignoring(lineItem);
            ignoring(mockFutureProductInstanceFacade);
        }});

        final List<LineItemModel> lineItemModels = newArrayList(lineItem);

        final List<Map<String, String>> rows = factory.createChannelInfoSheetRows(lineItemModels);

        assertThat(rows.size(), is(1));
        assertThat(rows.get(0).get("ov-channel-info.originating-country"), is(site.country));
        assertThat(rows.get(0).get("ov-channel-info.city"), is(site.city));
        assertThat(rows.get(0).get("ov-channel-info.site-name"), is(site.name));
        assertThat(rows.get(0).get("ov-channel-info.site-id"), is(site.bfgSiteID));
    }

    @Test
    public void shouldGenerateEmptyValues() throws Exception {
        final FutureAssetPricesModel futureAssetPricesModel =
            aFutureAssetPricesModel()
                .with(aFutureAssetPricesDTO().build())
                .with(productIdentifierFacade)
                .build();
        final LineItemModel lineItem = context.mock(LineItemModel.class);
        context.checking(new Expectations() {{
            allowing(lineItem).getSite();
            will(returnValue(site));
            allowing(lineItem).getFutureAssetPricesModel();
            will(returnValue(futureAssetPricesModel));
            ignoring(lineItem);
            ignoring(mockFutureProductInstanceFacade);
        }});

        final List<LineItemModel> lineItemModels = newArrayList(lineItem);

        final List<Map<String, String>> rows = factory.createChannelInfoSheetRows(lineItemModels);
        assertThat(rows.size(), is(1));
        assertThat(rows.get(0).get("ov-channel-info.site-status"), is(""));
        assertThat(rows.get(0).get("ov-channel-info.number-of-ranges"), is(""));
        assertThat(rows.get(0).get("ov-channel-info.number-of-recommended-ranges"), is(""));
        assertThat(rows.get(0).get("ov-channel-info.requested-price-per-channel"), is(""));
        assertThat(rows.get(0).get("ov-channel-info.effective-discount"), is(""));
        assertThat(rows.get(0).get("ov-channel-info.eup-total-for-number-ranges"), is(""));
        assertThat(rows.get(0).get("ov-channel-info.one-time-revenue-on-number-ranges"), is(""));
    }

    @Ignore("Kiran/Kaizer - Fix when BCM is made product agnostic")
    @Test
    public void shouldGenerateTariffType() throws Exception {
        final PriceLineDTOFixture.Builder priceDTOFixture =
            aPriceLineDTO()
                .with(PriceType.ONE_TIME);
        final FutureAssetPricesDTO futureAssetPricesDTO =
            aFutureAssetPricesDTO()
                .withPriceLine(priceDTOFixture)
                .build();
        final FutureAssetPricesModel futureAssetPricesModel =
            aFutureAssetPricesModel()
                .with(futureAssetPricesDTO)
                .build();
        final LineItemModel lineItem = context.mock(LineItemModel.class);
        context.checking(new Expectations() {{
            allowing(lineItem).getSite();
            will(returnValue(site));
            allowing(lineItem).getFutureAssetPricesModel();
            will(returnValue(futureAssetPricesModel));
            ignoring(lineItem);
            ignoring(mockFutureProductInstanceFacade);
        }});

        final List<LineItemModel> lineItemModels = newArrayList(lineItem);

        final List<Map<String, String>> rows = factory.createChannelInfoSheetRows(lineItemModels);

        assertThat(rows.size(), is(1));
        assertThat(rows.get(0).get("ov-channel-info.tariff-type"), is("Direct"));
    }

    @Test
    public void shouldGenerateNumberOfChannels() throws Exception {
        final LineItemModel lineItem = context.mock(LineItemModel.class);
        final LineItemId lineItemId = context.mock(LineItemId.class);
        final FlattenedProductStructure mockFlattenedOneVoiceProductStructure = context.mock(FlattenedProductStructure.class);

        context.checking(new Expectations() {{
            allowing(lineItem).getSite();
            will(returnValue(site));
            allowing(lineItem).getFutureAssetPricesModel();
            will(returnValue(aFutureAssetPricesModel().with(productIdentifierFacade).build()));
            allowing(lineItem).getLineItemId();
            will(returnValue(lineItemId));
            ignoring(lineItem);

            allowing(mockFutureProductInstanceFacade).getProductInstances(lineItemId);
            will(returnValue(mockFlattenedOneVoiceProductStructure));

            allowing(mockFlattenedOneVoiceProductStructure).firstAttributeValueFor(ProductCodes.OnevoiceOptions.productCode(),
                                                                                   OneVoiceConfiguration.BasicMPLS.OneVoiceOptions.NUMBER_VOICE_CHANNELS);
            will(returnValue("3"));
        }});

        final List<LineItemModel> lineItemModels = newArrayList(lineItem);

        final List<Map<String, String>> rows = factory.createChannelInfoSheetRows(lineItemModels);

        context.assertIsSatisfied();
        assertThat(rows.size(), is(1));
        assertThat(rows.get(0).get("ov-channel-info.number-of-channels"), is("3"));
    }

    @Test
    public void shouldGeneratePricingInformationForIndirectUser() throws Exception {
        List<Long> configPpsrIds = newArrayList(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_CONFIG.ppsrId(),
                                                OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_CONFIG.ppsrId(),
                                                OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_CONFIG.ppsrId());

        List<Long> subscriptionPpsrIds = newArrayList(OneVoicePriceTariff.GLOBAL_DIRECT_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId(),
                                                      OneVoicePriceTariff.GLOBAL_DIRECT_LITE_CHANNEL_SUBSCRIPTION.ppsrId(),
                                                      OneVoicePriceTariff.GLOBAL_INCLUSIVE_SINGLE_CHANNEL_SUBSCRIPTION.ppsrId());

        for (Long configPpsrId : configPpsrIds) {
            for (Long subscriptionPpsrId : subscriptionPpsrIds) {
                withIndirectUser();
                assertPricingInformationGenerated(configPpsrId, subscriptionPpsrId);
            }
        }
    }

    private void assertPricingInformationGenerated(Long configPpsrId, Long subscriptionPpsrId) {
        final FutureAssetPricesDTO futureAssetPricesDTO =
            aFutureAssetPricesDTO()
                .withPriceLine(aPriceLineDTO()
                                   .withPpsrId(configPpsrId)
                                   .with(PriceType.ONE_TIME)
                                   .withEupPrice(33.32)
                                   .withChargePrice(13.62)
                                   .withChargePriceDiscount(10.12345))
                .withPriceLine(aPriceLineDTO()
                                   .withPpsrId(subscriptionPpsrId)
                                   .with(PriceType.RECURRING)
                                   .withEupPrice(44.42)
                                   .withChargePrice(55.52)
                                   .withChargePriceDiscount(20.12345))
                .build();
        final FutureAssetPricesModel futureAssetPricesModel =
            aFutureAssetPricesModel()
                .with(futureAssetPricesDTO)
                .with(productIdentifierFacade)
                .with(pricingConfig)
                .build();
        final LineItemModel lineItem = context.mock(LineItemModel.class);
        final LineItemId lineItemId = context.mock(LineItemId.class);
        final FlattenedProductStructure mockFlattenedOneVoiceProductStructure = context.mock(FlattenedProductStructure.class);
        context.checking(new Expectations() {{
            allowing(lineItem).getSite();
            will(returnValue(site));
            allowing(lineItem).getFutureAssetPricesModel();
            will(returnValue(futureAssetPricesModel));
            allowing(lineItem).getLineItemId();
            will(returnValue(lineItemId));
            ignoring(lineItem);
            allowing(mockFutureProductInstanceFacade).getProductInstances(lineItemId);
            will(returnValue(mockFlattenedOneVoiceProductStructure));
            allowing(mockFlattenedOneVoiceProductStructure).firstAttributeValueFor(ProductCodes.OnevoiceOptions.productCode(),
                                                                                   OneVoiceConfiguration.BasicMPLS.OneVoiceOptions.NUMBER_VOICE_CHANNELS);
            will(returnValue("3"));
        }});

        final List<LineItemModel> lineItemModels = newArrayList(lineItem);

        final List<Map<String, String>> rows = factory.createChannelInfoSheetRows(lineItemModels);

        assertThat(rows.size(), is(1));
        assertThat(rows.get(0).get("ov-channel-info.access-type"), is("MPLS"));
        assertThat(rows.get(0).get("ov-channel-info.config-rrp-total"), is("33.32"));
        assertThat(rows.get(0).get("ov-channel-info.config-ptp-total"), is("13.62"));
        assertThat(rows.get(0).get("ov-channel-info.config-discount"), is("0.1012345"));
        assertThat(rows.get(0).get("ov-channel-info.subscription-rrp-total"), is("44.42"));
        assertThat(rows.get(0).get("ov-channel-info.subscription-ptp-total"), is("55.52"));
        assertThat(rows.get(0).get("ov-channel-info.subscription-discount"), is("0.2012345"));
        assertThat(rows.get(0).get("ov-channel-info.config-rrp-per-channel"), is("11.11"));
        assertThat(rows.get(0).get("ov-channel-info.config-ptp-per-channel"), is("4.54"));
        assertThat(rows.get(0).get("ov-channel-info.subscription-rrp-per-channel"), is("14.81"));
        assertThat(rows.get(0).get("ov-channel-info.subscription-ptp-per-channel"), is("18.51"));
    }

    private void withIndirectUser() {
        UserContextManager.setCurrent(anIndirectUserContext().build());
    }

    private void withDirectUser() {
        UserContextManager.setCurrent(aDirectUserContext().build());
    }
}


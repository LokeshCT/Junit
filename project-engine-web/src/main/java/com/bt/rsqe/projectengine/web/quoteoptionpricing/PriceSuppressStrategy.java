package com.bt.rsqe.projectengine.web.quoteoptionpricing;

import com.bt.rsqe.customerinventory.dto.PriceLineDTO;
import com.bt.rsqe.domain.bom.parameters.Losb;
import com.bt.rsqe.domain.product.chargingscheme.PricingStrategy;
import com.bt.rsqe.domain.product.chargingscheme.ProductChargingScheme;
import com.bt.rsqe.domain.project.PriceLine;
import com.bt.rsqe.domain.project.TariffType;
import com.bt.rsqe.enums.PriceCategory;
import com.bt.rsqe.pricing.config.dto.BillingTariffRulesetConfig;
import com.bt.rsqe.pricing.config.dto.PricingConfig;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;

public enum PriceSuppressStrategy {
    SummarySheet {
        @Override
        public List<PriceLine> suppressPriceCostLines(final List<ProductChargingScheme> chargingSchemes, List<PriceLine> priceLines) {
            if (!hasChargingSchemes(chargingSchemes)) {
                return priceLines;
            }
            return Lists.newArrayList(Iterables.filter(priceLines, new Predicate<PriceLine>() {
                @Override
                public boolean apply(PriceLine priceLine) {
                    if (!PriceCategory.COST.getLabel().equals(priceLine.getTariffType())) {
                        Optional<ProductChargingScheme> chargingSchemeOptional = chargingScheme(chargingSchemes, priceLine.getChargingSchemeName());
                        if (chargingSchemeOptional.isPresent() && isAvailableIn(chargingSchemeOptional.get().getPriceVisibility(), ProductChargingScheme.PriceVisibility.Customer)) {
                            return true;
                        }
                    }
                    return false;
                }
            }));
        }

        @Override
        public List<PriceLineDTO> suppressPriceCostLineDTOs(Optional<PricingConfig> pricingConfig, List<ProductChargingScheme> chargingSchemes, List<PriceLineDTO> priceLineDTOs) {
            return priceLineDTOs;
        }
    },
    DetailedSheet {
        @Override
        public List<PriceLine> suppressPriceCostLines(final List<ProductChargingScheme> chargingSchemes, List<PriceLine> priceLines) {
            if (!hasChargingSchemes(chargingSchemes)) {
                return priceLines;
            }
            return Lists.newArrayList(Iterables.filter(priceLines, new Predicate<PriceLine>() {
                @Override
                public boolean apply(PriceLine priceLine) {
                    if (!PriceCategory.COST.getLabel().equals(priceLine.getTariffType())) {
                        Optional<ProductChargingScheme> chargingSchemeOptional = chargingScheme(chargingSchemes, priceLine.getChargingSchemeName());
                        if (chargingSchemeOptional.isPresent() && isAvailableIn(chargingSchemeOptional.get().getPriceVisibility(), ProductChargingScheme.PriceVisibility.Customer, ProductChargingScheme.PriceVisibility.Sales)) {
                            return true;
                        }/*Optional<ProductChargingScheme> chargingSchemeOptional = chargingScheme(chargingSchemes, priceLine.getChargingSchemeName());
                        if (chargingSchemeOptional.isPresent() && !(chargingSchemeOptional.get().getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Hidden)) &&
                            !(chargingSchemeOptional.get().getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Customer) && chargingSchemeOptional.get().getPricingStrategy().equals(PricingStrategy.Aggregation) )  ) {
                            return true;
                        }*/
                    }
                    return false;
                }
            }));
        }

        @Override
        public List<PriceLineDTO> suppressPriceCostLineDTOs(Optional<PricingConfig> pricingConfig, List<ProductChargingScheme> chargingSchemes, List<PriceLineDTO> priceLineDTOs) {
            return priceLineDTOs;
        }
    },
    UI_COSTS {
        @Override
        public List<PriceLine> suppressPriceCostLines(List<ProductChargingScheme> chargingSchemes, List<PriceLine> priceLines) {
            throw new UnsupportedOperationException();
        }

        /**
         * Despite the method name this method WILL actually return you costs!
         */
        @Override
        public List<PriceLineDTO> suppressPriceCostLineDTOs(Optional<PricingConfig> pricingConfigOp, List<ProductChargingScheme> chargingSchemes, List<PriceLineDTO> priceLineDTOs) {
            if(!pricingConfigOp.isPresent()) {
                throw new UnsupportedOperationException("Can't suppress costs without pricing config");
            }

            final PricingConfig pricingConfig = pricingConfigOp.get();

            return newArrayList(Iterables.filter(priceLineDTOs, new Predicate<PriceLineDTO>() {
                @Override
                public boolean apply(PriceLineDTO input) {
                    final boolean isCost = TariffType.COST.equals(TariffType.forFriendlyName(input.getTariffType()));

                    if(isCost) {
                        // Costs are only visible on the UI if they can be discounted
                        final Optional<BillingTariffRulesetConfig> tariff = pricingConfig.getTariffFor(input.getChargingSchemeName(), input.getPmfId());
                        return tariff.isPresent() && tariff.get().isCostDiscountApplicable();
                    }
                    return false;
                }
            }));
        }
    },
    UI_PRICES {
        @Override
        public List<PriceLine> suppressPriceCostLines(List<ProductChargingScheme> chargingSchemes, List<PriceLine> priceLines) {
            return priceLines;
        }

        @Override
        public List<PriceLineDTO> suppressPriceCostLineDTOs(Optional<PricingConfig> pricingConfig, final List<ProductChargingScheme> chargingSchemes, List<PriceLineDTO> priceLineDTOs) {
            if (!hasChargingSchemes(chargingSchemes)) {
                return priceLineDTOs;
            }
            return Lists.newArrayList(Iterables.filter(priceLineDTOs, new Predicate<PriceLineDTO>() {
                @Override
                public boolean apply(PriceLineDTO priceLineDTO) {
                    if (!PriceCategory.COST.getLabel().equals(priceLineDTO.getTariffType())) {

                        Optional<ProductChargingScheme> chargingSchemeOptional = chargingScheme(chargingSchemes, priceLineDTO.getChargingSchemeName());

                        if(chargingSchemeOptional.isPresent()) {
                            if (isAvailableIn(chargingSchemeOptional.get().getPriceVisibility(), ProductChargingScheme.PriceVisibility.Customer, ProductChargingScheme.PriceVisibility.Sales)) {
                                return true;
                            } else if(chargingSchemeOptional.get().getPricingStrategy().equals(PricingStrategy.ManualPricing)) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }));
        }
    },
    OFFERS_UI {
        @Override
        public List<PriceLine> suppressPriceCostLines(List<ProductChargingScheme> chargingSchemes, List<PriceLine> priceLines) {
            return priceLines;
        }

        @Override
        public List<PriceLineDTO> suppressPriceCostLineDTOs(Optional<PricingConfig> pricingConfig, final List<ProductChargingScheme> chargingSchemes, List<PriceLineDTO> priceLineDTOs) {
            if (!hasChargingSchemes(chargingSchemes)) {
                return priceLineDTOs;
            }
            return Lists.newArrayList(Iterables.filter(priceLineDTOs, new Predicate<PriceLineDTO>() {
                @Override
                public boolean apply(PriceLineDTO priceLineDTO) {
                    if (!PriceCategory.COST.getLabel().equals(priceLineDTO.getTariffType()) && Losb.yes.toString().equals(priceLineDTO.getLosb())) {

                        Optional<ProductChargingScheme> chargingSchemeOptional = chargingScheme(chargingSchemes, priceLineDTO.getChargingSchemeName());
                        if (chargingSchemeOptional.isPresent() && isAvailableIn(chargingSchemeOptional.get().getPriceVisibility(), ProductChargingScheme.PriceVisibility.Customer, ProductChargingScheme.PriceVisibility.Sales)) {
                            return true;
                        }
                    }
                    return false;
                }
            }));
        }
    },
    BCMSheet {
        @Override
        public List<PriceLine> suppressPriceCostLines(final List<ProductChargingScheme> chargingSchemes, List<PriceLine> priceLines) {
            if (!hasChargingSchemes(chargingSchemes)) {
                return priceLines;
            }
            return Lists.newArrayList(Iterables.filter(priceLines, new Predicate<PriceLine>() {
                @Override
                public boolean apply(PriceLine priceLine) {
                    if (!PriceCategory.COST.getLabel().equals(priceLine.getTariffType())) {
                        Optional<ProductChargingScheme> chargingSchemeOptional = chargingScheme(chargingSchemes, priceLine.getChargingSchemeName());
                        if (chargingSchemeOptional.isPresent() && isAvailableIn(chargingSchemeOptional.get().getPriceVisibility(), ProductChargingScheme.PriceVisibility.Customer, ProductChargingScheme.PriceVisibility.Sales)
                            ) {
                            if (!(isAvailableIn(chargingSchemeOptional.get().getPricingStrategy(), PricingStrategy.Aggregation)
                                  && chargingSchemeOptional.get().getPriceVisibility().equals(ProductChargingScheme.PriceVisibility.Customer))) {
                                return true;
                            }
                        }
                    }
                    return false;
                }
            }));
        }

        @Override
        public List<PriceLineDTO> suppressPriceCostLineDTOs(Optional<PricingConfig> pricingConfig, List<ProductChargingScheme> chargingSchemes, List<PriceLineDTO> priceLineDTOs) {
            return priceLineDTOs;
        }
    },
    None {
        @Override
        public List<PriceLine> suppressPriceCostLines(List<ProductChargingScheme> chargingSchemes, List<PriceLine> priceLines) {
            return priceLines;
        }

        @Override
        public List<PriceLineDTO> suppressPriceCostLineDTOs(Optional<PricingConfig> pricingConfig, List<ProductChargingScheme> chargingSchemes, List<PriceLineDTO> priceLineDTOs) {
            return priceLineDTOs;
        }
    };

    private static boolean hasChargingSchemes(List<ProductChargingScheme> chargingSchemes) {
        return isNotNull(chargingSchemes) && !chargingSchemes.isEmpty();
    }

    // TODO rename to be more generic like suppressPriceLines
    public abstract List<PriceLine> suppressPriceCostLines(List<ProductChargingScheme> chargingSchemes, List<PriceLine> priceLines);

    // TODO rename to be more generic like suppressPriceLineDTOs
    public abstract List<PriceLineDTO> suppressPriceCostLineDTOs(Optional<PricingConfig> pricingConfig, List<ProductChargingScheme> chargingSchemes, List<PriceLineDTO> priceLineDTOs);

    private static Optional<ProductChargingScheme> chargingScheme(List<ProductChargingScheme> chargingSchemes, final String priceChargingScheme) {
        return Iterables.tryFind(chargingSchemes, new Predicate<ProductChargingScheme>() {
            @Override
            public boolean apply(ProductChargingScheme productChargingScheme) {
                return productChargingScheme.getName().equalsIgnoreCase(priceChargingScheme);
            }
        });
    }

    private static boolean isAvailableIn(final ProductChargingScheme.PriceVisibility priceVisibility, ProductChargingScheme.PriceVisibility... priceVisibilities) {
        Optional<ProductChargingScheme.PriceVisibility> priceVisibilityOptional = Iterables.tryFind(Arrays.asList(priceVisibilities), new Predicate<ProductChargingScheme.PriceVisibility>() {
            @Override
            public boolean apply(ProductChargingScheme.PriceVisibility visibility) {
                return visibility.equals(priceVisibility);
            }
        });

        return priceVisibilityOptional.isPresent();
    }

    private static boolean isAvailableIn(final PricingStrategy pricingStrategy, PricingStrategy... pricingStrategies) {
        Optional<PricingStrategy> pricingStrategiesOptional = Iterables.tryFind(Arrays.asList(pricingStrategies), new Predicate<PricingStrategy>() {
            @Override
            public boolean apply(PricingStrategy strategy) {
                return strategy.equals(pricingStrategy);
            }
        });

        return pricingStrategiesOptional.isPresent();
    }
}

package com.bt.rsqe.projectengine.web.model.modelfactory;

import com.bt.rsqe.customerinventory.dto.ProjectedUsageDTO;

import com.bt.rsqe.projectengine.web.facades.SpecialPriceBookFacade;
import com.bt.rsqe.projectengine.web.model.ProjectedUsageModel;
import com.bt.rsqe.projectengine.web.model.SpecialPriceBookModel;
import com.bt.rsqe.utils.countries.Countries;
import com.bt.rsqe.utils.countries.Country;

public class ProjectedUsageModelFactory {

    private SpecialPriceBookFacade specialPriceBookModelFactory;

    public ProjectedUsageModelFactory(SpecialPriceBookFacade specialPriceBookFacade) {
        this.specialPriceBookModelFactory = specialPriceBookFacade;
    }

    public ProjectedUsageModel create(ProjectedUsageDTO projectedUsageDTO, String quoteOptionId, String countryStr) {
        final SpecialPriceBookModel specialPriceBookModel = specialPriceBookModelFactory.get(quoteOptionId);
        Countries countries = new Countries();
        Country originCountry = countries.byDisplayName(countryStr);
        return new ProjectedUsageModel(projectedUsageDTO, specialPriceBookModel, originCountry);
    }
}

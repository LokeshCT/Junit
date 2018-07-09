package com.bt.rsqe.ape.source;

import com.bt.rsqe.ape.dto.AddressReferenceDTO;
import com.bt.rsqe.ape.dto.BritishAddressDTO;
import com.bt.rsqe.ape.dto.EFMAddressDTO;
import com.bt.rsqe.ape.dto.OnnetAvailabilityStatus;
import com.bt.rsqe.ape.dto.OnnetBuildingAvailabilityPerSiteDTO;
import com.bt.rsqe.ape.dto.OnnetBuildingDTO;
import com.bt.rsqe.ape.dto.OnnetBuildingsPerSiteWithEFMDTO;
import com.bt.rsqe.ape.dto.SupplierCheckRequest;
import com.bt.rsqe.ape.dto.SupplierSite;
import com.bt.rsqe.ape.dto.TechnologysDTO;
import com.bt.rsqe.ape.repository.APEQrefJPARepository;
import com.bt.rsqe.ape.repository.entities.AddressReferenceEntity;
import com.bt.rsqe.ape.repository.entities.BritishAddressEntity;
import com.bt.rsqe.ape.repository.entities.EFMAddressEntity;
import com.bt.rsqe.ape.repository.entities.OnnetAvailabilityEntity;
import com.bt.rsqe.ape.repository.entities.OnnetBuildingEntity;
import com.bt.rsqe.ape.repository.entities.OnnetBuildingsWithEFMEntity;
import com.bt.rsqe.ape.repository.entities.SupplierCheckClientRequestEntity;
import com.bt.rsqe.ape.repository.entities.SupplierRequestSiteEntity;
import com.bt.rsqe.ape.repository.entities.TechnologysEntity;
import com.bt.rsqe.customerrecord.SiteDTO;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static com.bt.rsqe.ape.constants.SupplierProductConstants.*;
import static com.bt.rsqe.ape.dto.OnnetAvailabilityType.*;
import static com.bt.rsqe.ape.dto.SupplierStatus.*;
import static com.bt.rsqe.ape.source.SupplierProductHelper.*;
import static com.bt.rsqe.ape.source.SupplierProductStore.*;
import static com.google.common.base.Strings.isNullOrEmpty;
import static com.google.common.collect.Lists.*;

/**
 * Created by 605875089 on 06/02/2016.
 */
public class OnnetDetailsOrchestrator {

    private APEQrefJPARepository repository;

    public OnnetDetailsOrchestrator(APEQrefJPARepository repository) {
        this.repository = repository;
    }

    private static OnnetDetailsOrchestratorLogger logger = LogFactory.createDefaultLogger(OnnetDetailsOrchestratorLogger.class);


    private List<OnnetAvailabilityEntity> getNonExistingSupplierSitesForOnNet(List<OnnetAvailabilityEntity> supportedSites, final List<Long> existingSites) {
        return newArrayList(Iterables.filter(supportedSites, new Predicate<OnnetAvailabilityEntity>() {
            @Override
            public boolean apply(OnnetAvailabilityEntity input) {
                return !existingSites.contains(BigDecimal.valueOf(input.getSiteId()));
            }
        }));
    }

    public List<SiteDTO> filterAvailableOnNetSites(List<SiteDTO> availableSites, final List<Long> existingSites) {
        return newArrayList(Iterables.filter(availableSites, new Predicate<SiteDTO>() {
            @Override
            public boolean apply(SiteDTO input) {
                return existingSites.contains(BigDecimal.valueOf(input.getSiteId().getValue()));
            }
        }));
    }

    public List<OnnetAvailabilityStatus> getOnNetAvailabilityEntityForAvailableSites(final List<SiteDTO> filteredSites) {

        return newArrayList(transform(newArrayList(filteredSites), new Function<SiteDTO, OnnetAvailabilityStatus>() {
            @Override
            public OnnetAvailabilityStatus apply(SiteDTO input) {
                OnnetAvailabilityEntity resultEntity = getOnNetAvailabilityEntity(input.getSiteId().getValue());
                return new OnnetAvailabilityStatus(resultEntity.getSiteId(), getAvailabilityTypeById(resultEntity.getOnNetAvailabilityTypeId()).getStatus(), resultEntity.getFailureReason());
            }
        }));
    }


    private List<OnnetBuildingEntity> toOnnetBuildingEntity(List<OnnetBuildingDTO> onnetBuildingDTOList, final OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity) {
        return newArrayList(transform(newArrayList(onnetBuildingDTOList), new Function<OnnetBuildingDTO, OnnetBuildingEntity>() {
            @Override
            public OnnetBuildingEntity apply(OnnetBuildingDTO input) {
                return new OnnetBuildingEntity(input.getFloorName(), input.getBuildingCode(), input.getAddress(), input.getStreetNumber(), input.getStreetName(),
                                               input.getPostCode(), input.getCity(), input.getState(), input.getCountry(), input.getKgi(), input.getAccuracy(),
                                               input.getLatitude(), input.getLongitude(), input.getDistance(), input.getGroupNumber(), null, null, null, null, new Date(), onnetBuildingsWithEFMEntity);
            }
        }));

    }


    private List<EFMAddressEntity> toEFMAddressEntity(List<EFMAddressDTO> efmAddressDTOList, final OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity) {
        return newArrayList(transform(newArrayList(efmAddressDTOList), new Function<EFMAddressDTO, EFMAddressEntity>() {
            @Override
            public EFMAddressEntity apply(EFMAddressDTO input) {
                BritishAddressDTO brAdDto = input.getBritishAddressDTO();
                AddressReferenceDTO adRefDto = input.getAddressReferenceDTO();

                EFMAddressEntity efmAddressEntity = new EFMAddressEntity(null, new Date(), onnetBuildingsWithEFMEntity);
                BritishAddressEntity britishAddressEntity = new BritishAddressEntity(brAdDto.getSubPremises(), brAdDto.getPremisesName(), brAdDto.getThoroughfareName(), brAdDto.getPostTown(), brAdDto.getCounty(), brAdDto.getPostCode(), new Date(), efmAddressEntity);
                AddressReferenceEntity addressReferenceEntity = new AddressReferenceEntity(adRefDto.getRefNum(), adRefDto.getQualifier(), adRefDto.getDistrictCode(), efmAddressEntity, new Date());
                List<TechnologysEntity> technologysEntityList = toTechnologysEntityList(adRefDto.getListOfTechnology(), addressReferenceEntity);

                addressReferenceEntity.setListOfTechnology(technologysEntityList);

                efmAddressEntity.setBritishAddressEntity(britishAddressEntity);
                efmAddressEntity.setAddressReferenceEntity(addressReferenceEntity);

                return efmAddressEntity;
            }
        }));
    }

    private List<TechnologysEntity> toTechnologysEntityList(List<TechnologysDTO> listOfTechnology, final AddressReferenceEntity addressReferenceEntity) {
        return newArrayList(transform(newArrayList(listOfTechnology), new Function<TechnologysDTO, TechnologysEntity>() {
            @Override
            public TechnologysEntity apply(TechnologysDTO input) {
                return new TechnologysEntity(input.getTechnology(), new Date(), addressReferenceEntity);
            }
        }));

    }

    public void prepareOnNetRequestData(SupplierCheckRequest request) throws Exception {


        SupplierCheckClientRequestEntity clientRequestDb = getSupplierCheckClientRequest(request.getParentRequestId());
        SupplierCheckClientRequestEntity clientRequest = null;
        if (clientRequestDb == null) {
            clientRequest = new SupplierCheckClientRequestEntity(request.getParentRequestId(), request.getClientCallbackUri(), request.getTriggerType(), request.getAutoTrigger(),
                                                                 request.getSourceSystemName(), request.getUser(), Long.parseLong(request.getCustomerId()), InProgress.value(), timestamp(), timestamp());
        } else {
            clientRequest = clientRequestDb;
        }

        List<String> supportedCountries = getDslEfmSupportedCountries();
        List<SupplierRequestSiteEntity> siteEntities = newArrayList();
        List<SupplierSite> supportedSites = newArrayList();
        List<OnnetAvailabilityEntity> onnetAvailabilityEntity = newArrayList();
        for (SupplierSite supplierSite : request.getSupplierSites()) {

            String siteLevelStatus = supportedCountries.contains(supplierSite.getCountryISOCode()) ? InProgress.value() : NotSupported.value();
            if (InProgress.value().equalsIgnoreCase(siteLevelStatus)) {
                supportedSites.add(supplierSite);
            }
            siteEntities.add(new SupplierRequestSiteEntity(String.valueOf(supplierSite.getSiteId()), NotSupported.value().equalsIgnoreCase(siteLevelStatus) ? Completed.value() : InProgress.value(), siteLevelStatus, timestamp(), timestamp(), clientRequest));
            onnetAvailabilityEntity.add(new OnnetAvailabilityEntity(supplierSite.getSiteId(),
                                                                    Long.parseLong(request.getCustomerId()),
                                                                    supplierSite.getSiteName(),
                                                                    supplierSite.getCountryISOCode(),
                                                                    supplierSite.getCountryName(),
                                                                    supplierSite.getCity(),
                                                                    null,
                                                                    null,
                                                                    InProgress.value(),
                                                                    OrangeIcon.getId(),
                                                                    null));
        }
        clientRequest.setSupplierRequestSiteEntities(siteEntities);
        storeClientRequest(clientRequest);
        List<Long> existingSites = getExistingSitesAfterExcludingFailedSitesForOnNet(Long.parseLong(request.getCustomerId()));
        if (onnetAvailabilityEntity.size() > 0) {
            storeOnNetAvailabilityList(getNonExistingSupplierSitesForOnNet(onnetAvailabilityEntity, existingSites));
        }

        if (SYSTEM.equalsIgnoreCase(request.getTriggerType())) {
            filterExistingSitesFromRequest(request, existingSites);
        }
    }

    public List<Long> getExistingSitesAfterExcludingFailedSitesForOnNet(Long customerId) throws Exception {
        return repository.getExistingSitesAfterExcludingFailedSitesForOnNet(customerId);
    }


    public List<Long> getExistingOnNetSites(Long customerId) throws Exception {
        return repository.getExistingOnNetSites(customerId);
    }


    public void updateOnNetAvailability(List<OnnetBuildingAvailabilityPerSiteDTO> onnetBuildingAvailabilityPerSiteDTOList) {
        List<OnnetAvailabilityEntity> onnetAvailabilityEntityList = newArrayList();
        for (OnnetBuildingAvailabilityPerSiteDTO onnetBuildingAvailabilityPerSiteDTO : onnetBuildingAvailabilityPerSiteDTOList) {
            OnnetAvailabilityEntity onnetAvailabilityEntity = repository.getOnNetAvailabilityEntity(Long.parseLong(onnetBuildingAvailabilityPerSiteDTO.getSiteID()));
            if ("Y".equalsIgnoreCase(onnetBuildingAvailabilityPerSiteDTO.getOnnetAvailable())) {
                onnetAvailabilityEntity.setOnNetAvailability(Completed.value());
                onnetAvailabilityEntity.setOnNetAvailabilityTypeId(GreyIcon.getId());
            } else if ("N".equalsIgnoreCase(onnetBuildingAvailabilityPerSiteDTO.getOnnetAvailable())) {
                onnetAvailabilityEntity.setOnNetAvailability(NotSupported.value());
                onnetAvailabilityEntity.setOnNetAvailabilityTypeId(GreyRedCrossIcon.getId());
            }
            onnetAvailabilityEntityList.add(onnetAvailabilityEntity);
            // repository.updateOnNetAvailability(Long.parseLong(onnetBuildingAvailabilityPerSiteDTO.getSiteID()), onnetBuildingAvailabilityPerSiteDTO.getOnnetAvailable());
        }
        storeOnNetAvailabilityList(onnetAvailabilityEntityList);
    }

    public void saveOnnetBuildingsWithEFMEntity(OnnetBuildingsPerSiteWithEFMDTO request) {
        repository.saveOnnetBuildingsWithEFMEntity(getUpdatedOnnetDetailsEntity(request));
    }


    private OnnetBuildingsWithEFMEntity getUpdatedOnnetDetailsEntity(OnnetBuildingsPerSiteWithEFMDTO request) {

        OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity = repository.getOnnetBuildingsWithEFMEntity(Long.parseLong(request.getBfgSiteId()));

        final List<String> buildingCodeList = newArrayList();
        final List<String> subPremisesList = newArrayList();
        for (OnnetBuildingDTO onnetBuildingDTO : request.getOnnetbuildings()) {
            buildingCodeList.add(onnetBuildingDTO.getBuildingCode());
        }
        for (EFMAddressDTO efmAddressDTO : request.getListOfAddress()) {
            subPremisesList.add(efmAddressDTO.getBritishAddressDTO().getSubPremises());
        }

        List<OnnetBuildingEntity> updatedOnnetBuildingEntityList = newArrayList(Iterables.filter(onnetBuildingsWithEFMEntity.getOnnetBuildingEntityList(), new Predicate<OnnetBuildingEntity>() {
            @Override
            public boolean apply(OnnetBuildingEntity input) {
                if (buildingCodeList.contains(input.getBuildingCode())) {
                    input.setSelection("Yes");
                    return true;
                } else {
                    input.setSelection(null);
                    return true;
                }
            }
        }));
        List<EFMAddressEntity> UpdatedEFMAddressEntityList = newArrayList(Iterables.filter(onnetBuildingsWithEFMEntity.getEfmAddressEntityList(), new Predicate<EFMAddressEntity>() {
            @Override
            public boolean apply(EFMAddressEntity input) {
                if (subPremisesList.contains(input.getBritishAddressEntity().getSubPremises())) {
                    input.setSelection("Yes");
                    return true;
                } else {
                    input.setSelection(null);
                    return true;
                }
            }
        }));

        onnetBuildingsWithEFMEntity.setOnnetBuildingEntityList(updatedOnnetBuildingEntityList);
        onnetBuildingsWithEFMEntity.setEfmAddressEntityList(UpdatedEFMAddressEntityList);

        return onnetBuildingsWithEFMEntity;
    }


    public OnnetAvailabilityEntity getOnNetAvailabilityEntity(long siteId) {
        return repository.getOnNetAvailabilityEntity(siteId);
    }


    public void storeOnNetAvailabilityList(List<OnnetAvailabilityEntity> siteEntityList) {
        for (OnnetAvailabilityEntity site : siteEntityList) {
            repository.save(site);
        }
    }


    public SupplierCheckClientRequestEntity getSupplierCheckClientRequest(String requestId) throws Exception {
        return repository.getSupplierCheckClientRequest(requestId);
    }

    public void storeOnnetBuildingsPerSiteWithEFMRespone(String siteId, List<OnnetBuildingDTO> onnetBuildingDTOList, List<EFMAddressDTO> efmAddressDTOList) {

        OnnetBuildingsWithEFMEntity onnetBuildingsWithEFMEntity = new OnnetBuildingsWithEFMEntity(Long.parseLong(siteId), new Date());

        List<OnnetBuildingEntity> onnetBuildingEntityList = toOnnetBuildingEntity(onnetBuildingDTOList, onnetBuildingsWithEFMEntity);
        List<EFMAddressEntity> efmAddressEntityList = toEFMAddressEntity(efmAddressDTOList, onnetBuildingsWithEFMEntity);

        onnetBuildingsWithEFMEntity.setOnnetBuildingEntityList(onnetBuildingEntityList);
        onnetBuildingsWithEFMEntity.setEfmAddressEntityList(efmAddressEntityList);

        repository.saveOnnetBuildingsWithEFMEntity(onnetBuildingsWithEFMEntity);
        // efmAddressEntityList


    }


    public OnnetBuildingsWithEFMEntity getOnnetBuildingsWithEFMEntity(String siteId) {
        return repository.getOnnetBuildingsWithEFMEntity(Long.parseLong(siteId));
    }

    private interface OnnetDetailsOrchestratorLogger {
        @Log(level = LogLevel.ERROR, format = "Error : '%s'")
        void error(Exception e);


    }

}

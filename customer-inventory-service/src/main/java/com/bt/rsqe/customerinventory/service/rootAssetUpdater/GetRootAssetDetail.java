package com.bt.rsqe.customerinventory.service.rootAssetUpdater;

import com.bt.rsqe.bfgfacade.repository.BfgRepository;
import com.bt.rsqe.bfgfacade.write.sp.IStoredProcedureFacade;
import com.bt.rsqe.client.Pmr;
import com.bt.rsqe.customerinventory.client.ProductInstanceClient;
import com.bt.rsqe.customerinventory.parameter.LineItemId;
import com.bt.rsqe.customerinventory.repository.jpa.entities.FutureAssetRelationshipEntity;
import com.bt.rsqe.customerinventory.service.client.domain.CIFAsset;
import com.bt.rsqe.customerinventory.service.repository.CIFAssetJPARepository;
import com.bt.rsqe.domain.bom.parameters.ProductSCode;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.pmr.client.PmrClient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.NoResultException;
import java.util.List;


public class GetRootAssetDetail {

    private static final Logger LOG = LoggerFactory.getLogger(GetRootAssetDetail.class);
    static boolean rootFlag = true;
    static ProductInstance rootProductInstance = null;

    public GetRootAssetDetail() {

    }

    public static String updateCustomerDetails(ProductInstanceClient productInstanceClient, CIFAssetJPARepository cifAssetJPARepository, BfgRepository bfgRepository, String customerId, IStoredProcedureFacade iStoredProcedureFacade, boolean onBoardFalg, PmrClient pmr) {

        String flag = "false";
        boolean cifassetflag=true;
        List<CIFAsset> cifAssetList = cifAssetJPARepository.getAssetDetails(customerId, true);
        if (cifAssetList.size() == 0) {
            flag = "Empty";

        } else {

            RootAssetDto rootAssetDto = new RootAssetDto();
            String packageInstanceId;

            ProductInstance currentLineItemInstance;


            rootProductInstance = getRootAssetDetail(productInstanceClient, pmr, cifAssetList);
            for (CIFAsset assetDetail : cifAssetList) {
                checkRootAsset(productInstanceClient, pmr, assetDetail);

                List<FutureAssetRelationshipEntity> futureAssetRelationshipall = cifAssetJPARepository.getRelationDetails(assetDetail.getAssetKey().getAssetId(), assetDetail.getLineItemId());
                LOG.info("Printing result set " +futureAssetRelationshipall);

                if(futureAssetRelationshipall.size()==0)
                {
                    cifassetflag=true;
                    LOG.info("Entering cif writing");
                    currentLineItemInstance = productInstanceClient.get(new LineItemId(assetDetail.getLineItemId()));


                    RootAssetConstants.OfferingType isWritableCandidate = RootAssetConstants.OfferingType.getByName(rootProductInstance.getSimpleProductOfferingType().toString());
                    if (!isWritableCandidate.equals(RootAssetConstants.OfferingType.INVALID_OfferingType)) {


                        rootAssetDto= setRootAssetParameters( isWritableCandidate, rootAssetDto, currentLineItemInstance, bfgRepository,null, assetDetail, customerId,cifassetflag);
                        flag = setRootDto(onBoardFalg, rootAssetDto, iStoredProcedureFacade);

                    }

                }


                else
                {
                    for (FutureAssetRelationshipEntity relation : futureAssetRelationshipall) {
                        LOG.info("Each Relation Name" + relation.getRelationshipName());
                        //to get the root asset details
                        cifassetflag=false;
                        currentLineItemInstance = productInstanceClient.get(new LineItemId(assetDetail.getLineItemId()));


                        RootAssetConstants.OfferingType isWritableCandidate = RootAssetConstants.OfferingType.getByName(rootProductInstance.getSimpleProductOfferingType().toString());
                        if (!isWritableCandidate.equals(RootAssetConstants.OfferingType.INVALID_OfferingType)) {


                            rootAssetDto= setRootAssetParameters( isWritableCandidate, rootAssetDto, currentLineItemInstance, bfgRepository,relation.getRelationshipType(), assetDetail, customerId,cifassetflag);
                            flag = setRootDto(onBoardFalg, rootAssetDto, iStoredProcedureFacade);

                        }

                    }
                }
            }
        }
        return flag;
    }

    public static RootAssetDto setRootAssetParameters(RootAssetConstants.OfferingType isWritableCandidate,RootAssetDto rootAssetDto,ProductInstance currentLineItemInstance,BfgRepository bfgRepository,String relation,CIFAsset assetDetail,String customerId,boolean cifflag)
    {
        String packageInstanceId;

        if (!isWritableCandidate.equals(RootAssetConstants.OfferingType.INVALID_OfferingType)) {

            rootAssetDto.setRelElementType(RootAssetConstants.OfferingShortName.getByName(currentLineItemInstance.getSimpleProductOfferingType().toString()));
            if(cifflag)
            {
                rootAssetDto.setRelElementId(Long.parseLong(currentLineItemInstance.getBfgAssetId()));
            }
            else
            {
                rootAssetDto.setRelElementId(Long.parseLong(assetDetail.getBfgAssetId()));
            }
            rootAssetDto.setElementRelationshipType(relation);
        rootAssetDto.setElementRelationshipType(relation);
            packageInstanceId = bfgRepository.getOfferingTypeInstanceId(BfgIdentifierLookUpQuery.getInventoryIdentifierQuery(rootProductInstance.getSimpleProductOfferingType()),
                                                                        rootProductInstance.getBfgAssetId());
            rootAssetDto.setRootElementIdentifier(packageInstanceId);
            rootAssetDto.setRootElementId(Long.parseLong(rootProductInstance.getBfgAssetId()));
            rootAssetDto.setRootElementType(RootAssetConstants.OfferingShortName.getByName(rootProductInstance.getSimpleProductOfferingType().toString()));
            rootAssetDto.setElementId(Long.parseLong(currentLineItemInstance.getBfgAssetId()));
            rootAssetDto.setElementType(RootAssetConstants.OfferingShortName.getByName(currentLineItemInstance.getSimpleProductOfferingType().toString()).toString());
            rootAssetDto.setElementSourceSystem(RootAssetConstants.SourceSystem);
            rootAssetDto.setElementInsPref(currentLineItemInstance.getProductIdentifier().getProductId());

            rootAssetDto.setCustomerId(Long.parseLong(customerId));
        }
        return rootAssetDto;
    }


    public static String setRootDto(boolean onBoardFalg, RootAssetDto rootAssetDto, IStoredProcedureFacade iStoredProcedureFacade) {

        String flag = "false";

        if (onBoardFalg) {
            CifCustomerOnBoardSPParameter cifCustomerOnBoardSPParameter = new CifCustomerOnBoardSPParameter(rootAssetDto);

            iStoredProcedureFacade.execute(cifCustomerOnBoardSPParameter);

            if (cifCustomerOnBoardSPParameter.isSuccess()) {
                LOG.info("Successfully Uploaded customer details to BFG");
                flag = "true";
            } else {
                LOG.error("Stored Procedure Execution failed! " + cifCustomerOnBoardSPParameter.errorCode + ":" + cifCustomerOnBoardSPParameter.errorMsg);
                //throw new RuntimeException(cifCustomerOnBoardSPParameter.errorCode + ":" + cifCustomerOnBoardSPParameter.errorMsg);
                flag = cifCustomerOnBoardSPParameter.errorCode + ":" + cifCustomerOnBoardSPParameter.errorMsg;
            }
        } else {
            CifCustomerDeltaServiceSPParameter cifCustomerDeltaServiceSPParameter = new CifCustomerDeltaServiceSPParameter(rootAssetDto);

            iStoredProcedureFacade.execute(cifCustomerDeltaServiceSPParameter);

            if (cifCustomerDeltaServiceSPParameter.isSuccess()) {
                LOG.info("Successfully On Boarded customer details to BFG");
                flag = "true";
            } else {
                LOG.error("Stored Procedure Execution failed! " + cifCustomerDeltaServiceSPParameter.errorCode + ":" + cifCustomerDeltaServiceSPParameter.errorMsg);
                //throw new RuntimeException(cifCustomerDeltaServiceSPParameter.errorCode + ":" + cifCustomerDeltaServiceSPParameter.errorMsg);
                flag = cifCustomerDeltaServiceSPParameter.errorCode + ":" + cifCustomerDeltaServiceSPParameter.errorMsg;
            }
        }

        return flag;
    }

    private static String getRelationShipType(String assetUniqueId, String lineItemId, CIFAssetJPARepository cifAssetJPARepository) {

        List<FutureAssetRelationshipEntity> futureAssetRelationshipEntityList = cifAssetJPARepository.getRelationDetails(assetUniqueId, lineItemId);
        if (futureAssetRelationshipEntityList.size() == 0) {
            return null;
        } else {
            return futureAssetRelationshipEntityList.get(0).getRelationshipType();
        }
    }

    public static ProductInstance getRootAssetDetail(ProductInstanceClient productInstanceClient, PmrClient pmr, List<CIFAsset> cifAssetList) {

        for (CIFAsset assetDetail : cifAssetList) {
            rootProductInstance = productInstanceClient.get(new LineItemId(assetDetail.getLineItemId()));
            Pmr.ProductOfferings productOffering = pmr.productOffering(ProductSCode.newInstance(rootProductInstance.getProductIdentifier().getProductId() == null ? null : rootProductInstance.getProductIdentifier().getProductId().toString()));

            if (rootProductInstance != null) {
                if (productOffering.get().isInFrontCatalogue() || productOffering.get().isSeparatelyModifiable()) {
                    return rootProductInstance;
                }
            }
        }

        return rootProductInstance;
    }

    public static ProductInstance checkRootAsset(ProductInstanceClient productInstanceClient, PmrClient pmr, CIFAsset assetDetail) {

        ProductInstance rootProductInstanceTemp;
        rootProductInstanceTemp = productInstanceClient.get(new LineItemId(assetDetail.getLineItemId()));
        Pmr.ProductOfferings productOffering = pmr.productOffering(ProductSCode.newInstance(rootProductInstanceTemp.getProductIdentifier().getProductId() == null ? null : rootProductInstanceTemp.getProductIdentifier().getProductId().toString()));

        if (rootProductInstanceTemp != null) {
            if (productOffering.get().isInFrontCatalogue() || productOffering.get().isSeparatelyModifiable()) {
                if ((rootProductInstanceTemp.getProductIdentifier().getProductId().equalsIgnoreCase(rootProductInstance.getProductIdentifier().getProductId())) && (rootProductInstanceTemp.getBfgAssetId().equalsIgnoreCase(rootProductInstance.getBfgAssetId()))) {
                    return rootProductInstance;
                } else {
                    rootProductInstance = rootProductInstanceTemp;
                }
            }
        }


        return rootProductInstance;
    }

    public String getCustomerId(String quoteOptionId, BfgRepository bfgRepository, CIFAssetJPARepository cifAssetJPARepository) {

        String customerId = cifAssetJPARepository.getAssetByQuoteOptionId(quoteOptionId);
        LOG.info("This customer customerId::::::" + customerId);
        boolean customerPresentFlag = bfgRepository.getCustomerPresent(RootAssetConstants.Customer_OnBoard_LookUp_Query, customerId);
        if (customerPresentFlag) {
            return customerId;
        } else {
            LOG.info("This customer is not OnBoarded");
            throw new NoResultException();
        }
    }
}

package com.bt.dsl.excel;

import com.bt.dsl.excel.constant.ApplicabilityEnum;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdAvailDTO;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdDetailDTO;
import com.bt.rsqe.utils.AssertObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 31/08/15
 * Time: 19:32
 * To change this template use File | Settings | File Templates.
 */
public class SacXlAccessTechAndProductsDetailsMap {
    private static final Logger LOG = LoggerFactory.getLogger(SacXlAccessTechAndProductsDetailsMap.class);
    private Map<String, List<SacXlSupplierProductDto>> map = new TreeMap<>();
    private List<String> accessOrder = null;

    public void add(String accessTechnology, List<SacXlSupplierProductDto> list) {
        Collections.sort(list);
        map.put(accessTechnology, list);
    }

    public List<SacXlSupplierProductDto> get(String accessTechnology) {
        return map.get(accessTechnology);
    }

    public List<String> getAccessTechnologySequence() {
        if (accessOrder == null && map != null) {
            this.accessOrder = new ArrayList<>(map.keySet());
            LOG.info("SAC Report Gen Access Technology Order ::" + accessOrder);
        }
        return accessOrder;
    }


    public static SacXlAccessTechAndProductsDetailsMap getInstance(List<SacSupplierProdAvailDTO> sacSupplierProdAvailDTOs) {
        SacXlAccessTechAndProductsDetailsMap mapDto = new SacXlAccessTechAndProductsDetailsMap();

        if (sacSupplierProdAvailDTOs == null || sacSupplierProdAvailDTOs.size() == 0) {
            return null;
        }

        for (SacSupplierProdAvailDTO prodAvailDTO : sacSupplierProdAvailDTOs) {
            SacSupplierProdDetailDTO suplierProductDetailDto = prodAvailDTO.getSacSupplierProdDetailDTO();
            String accessTechnology = suplierProductDetailDto.getAccessType() + " " + suplierProductDetailDto.getServiceVarient();
            //String accessEnum = String.getEnum(accessTechnology);
            if (!AssertObject.isEmpty(accessTechnology)) {
                List<SacXlSupplierProductDto> supplierProductList = mapDto.get(accessTechnology);
                if (supplierProductList == null) {
                    supplierProductList = new ArrayList<SacXlSupplierProductDto>();
                    mapDto.add(accessTechnology, supplierProductList);
                }
                SacXlSupplierProductDto newSacXlSupplierProductDto = new SacXlSupplierProductDto(suplierProductDetailDto);
                if (supplierProductList != null && newSacXlSupplierProductDto != null && newSacXlSupplierProductDto.getApplicability() == ApplicabilityEnum.YES) {
                    applyYesOrNo(supplierProductList, newSacXlSupplierProductDto);
                }
                supplierProductList.add(newSacXlSupplierProductDto);
                Collections.sort(supplierProductList);
            }

        }
        return mapDto;

    }

    private static void applyYesOrNo(List<SacXlSupplierProductDto> sacXlSupplierProductDtoList, SacXlSupplierProductDto dto) {
        for (SacXlSupplierProductDto productDtoOfList : sacXlSupplierProductDtoList) {
            if (productDtoOfList.getSupplierName().replace(" ", "").equalsIgnoreCase(dto.getSupplierName().replace(" ", "")) && productDtoOfList.getAccessSpeed().replace(" ", "").equalsIgnoreCase(dto.getAccessSpeed().replace(" ", ""))
                && productDtoOfList.getAccessSpeedUnit().replace(" ", "").equalsIgnoreCase(dto.getAccessSpeedUnit().replace(" ", ""))) {
                if (!AssertObject.areEmpty(productDtoOfList.getNoOfCopperPairs(), dto.getNoOfCopperPairs())) {
                    try {
                        int existingCopperPairNo = Integer.parseInt(productDtoOfList.getNoOfCopperPairs());
                        int newCopperPairNo = Integer.parseInt(dto.getNoOfCopperPairs());
                        if (existingCopperPairNo < newCopperPairNo) {
                            if (productDtoOfList.getApplicability() == dto.getApplicability()) {
                                dto.setApplicability(ApplicabilityEnum.NO);
                                continue;
                            } else if (productDtoOfList.getApplicability() == ApplicabilityEnum.YES) {
                                dto.setApplicability(ApplicabilityEnum.NO);
                                continue;
                            }
                        } else if (existingCopperPairNo > newCopperPairNo) {
                            if (dto.getApplicability() == productDtoOfList.getApplicability()) {
                                productDtoOfList.setApplicability(ApplicabilityEnum.NO);
                                continue;
                            } else if (dto.getApplicability() == ApplicabilityEnum.YES) {
                                productDtoOfList.setApplicability(ApplicabilityEnum.NO);
                                continue;
                            }
                        }

                    } catch (Exception e) {
                        //dont do any thing.
                    }
                }
            }
        }
    }

}

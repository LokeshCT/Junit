package com.bt.dsl.excel;

import com.bt.dsl.excel.constant.AccessTechnologyEnum;
import com.bt.dsl.excel.constant.ApplicabilityEnum;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdAvailDTO;
import com.bt.rsqe.ape.dto.sac.SacSupplierProdDetailDTO;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 607937181
 * Date: 03/09/15
 * Time: 12:57
 * To change this template use File | Settings | File Templates.
 */
public class AvailabilityCheckerXLWriterTest {
    @Before
    public void setUp() throws Exception {

    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testWriteToNewExcel() throws Exception {

        //OutputStream fos = new FileOutputStream("C:\\MySpace\\Workspace\\RSQE_trunk\\Outcome_rusult_5.xlsx");
        OutputStream fos = new ByteArrayOutputStream();
        AvailabilityCheckerXLWriter writer = new AvailabilityCheckerXLWriter(fos);

        SacXlRowDataModel dto = new SacXlRowDataModel();

        SacXlAccessTechAndProductsDetailsMap sacDto = new SacXlAccessTechAndProductsDetailsMap();
        List<SacXlSupplierProductDto> dslPrimiumDataList = new ArrayList<SacXlSupplierProductDto>();

        dslPrimiumDataList.add(createDtoObject("640","Kbps/kbps", "SFR", "1", "REFLEX 05S","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPrimiumDataList.add(createDtoObject("640","Kbps/kbps", "SFR", "2", "REFLEX 05S","Surfer 512 Display", ApplicabilityEnum.NO));
        dslPrimiumDataList.add(createDtoObject("1280","Kbps/kbps", "SFR", "1", "REFLEX 1S 2P","Surfer 512 Display", ApplicabilityEnum.NO));
        dslPrimiumDataList.add(createDtoObject("2048","Kbps/kbps", "SFR", "2", "REFLEX 2S 2P","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPrimiumDataList.add(createDtoObject("512","Kbps/kbps", "Orange", "1", "SDSL 1P 500K garanti (0,5gS)","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPrimiumDataList.add(createDtoObject("1024","Kbps/kbps", "Orange", "1", "SDSL 1P 1000K garanti (1gS)","Surfer 512 Display", ApplicabilityEnum.NO));

        List<SacXlSupplierProductDto> dslPlusDataList = new ArrayList<SacXlSupplierProductDto>();
        dslPlusDataList.add(createDtoObject("576/160","Kbps/kbps", "SFR", "1", "REFLEX 05A","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPlusDataList.add(createDtoObject("1216/320","Kbps/kbps", "SFR", "1", "REFLEX 1A","Surfer 512 Display", ApplicabilityEnum.NO));


        List<SacXlSupplierProductDto> dslStandardDataList = new ArrayList<SacXlSupplierProductDto>();
        dslStandardDataList.add(createDtoObject("512/128","Kbps/kbps", "SFR", "-", "Surfer 512","Surfer 512 Display", ApplicabilityEnum.NO));
        dslStandardDataList.add(createDtoObject("512/128","Kbps/kbps", "SFR", "-", "ACA 512","Surfer 512 Display", ApplicabilityEnum.YES));
        dslStandardDataList.add(createDtoObject("1024/256","Kbps/kbps", "SFR", "-", "Surfer 1024 Pro","Surfer 512 Display", ApplicabilityEnum.NORESPONSE));
        dslStandardDataList.add(createDtoObject("1024/256","Kbps/kbps", "SFR", "-", "ACA 1024","Surfer 512 Display", ApplicabilityEnum.NO));

        sacDto.add(AccessTechnologyEnum.DSLSTANDARD.toString(), dslStandardDataList);
        sacDto.add(AccessTechnologyEnum.DSLPREMIUM.toString(), dslPrimiumDataList);
        sacDto.add(AccessTechnologyEnum.DSLPLUS.toString(), dslPlusDataList);

        dto.setSacXlAccessAndProductsMapDto(sacDto);
        dto.setCountry("India");
        dto.setSiteName("site 1");
        dto.setPhoneNo("11111111111");

        //Second row start.

        SacXlRowDataModel dto1 = new SacXlRowDataModel();
        SacXlAccessTechAndProductsDetailsMap sacDto1 = new SacXlAccessTechAndProductsDetailsMap();

        List<SacXlSupplierProductDto> dslPrimiumDataList1 = new ArrayList<SacXlSupplierProductDto>();

        dslPrimiumDataList1.add(createDtoObject("2048","Kbps/kbps", "SFR", "2", "REFLEX 2S 2P","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPrimiumDataList1.add(createDtoObject("512","Kbps/kbps", "Orange", "1", "SDSL 1P 500K garanti (0,5gS)","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPrimiumDataList1.add(createDtoObject("1024","Kbps/kbps", "Orange", "1", "SDSL 1P 1000K garanti (1gS)","Surfer 512 Display", ApplicabilityEnum.NO));
        dslPrimiumDataList1.add(createDtoObject("640","Kbps/kbps", "SFR", "1", "REFLEX 05S","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPrimiumDataList1.add(createDtoObject("640","Kbps/kbps", "SFR", "2", "REFLEX 05S","Surfer 512 Display", ApplicabilityEnum.NO));
        dslPrimiumDataList1.add(createDtoObject("1280","Kbps/kbps", "SFR", "1", "REFLEX 1S 2P","Surfer 512 Display", ApplicabilityEnum.NO));

        List<SacXlSupplierProductDto> dslPlusDataList1 = new ArrayList<SacXlSupplierProductDto>();
        dslPlusDataList1.add(createDtoObject("576/160","Kbps/kbps", "SFR", "1", "REFLEX 05A","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPlusDataList1.add(createDtoObject("1216/320","Kbps/kbps", "SFR", "1", "REFLEX 1A","Surfer 512 Display", ApplicabilityEnum.NO));


        List<SacXlSupplierProductDto> dslStandardDataList1 = new ArrayList<SacXlSupplierProductDto>();
        dslStandardDataList1.add(createDtoObject("512/128","Kbps/kbps", "SFR", "-", "Surfer 512","Surfer 512 Display", ApplicabilityEnum.NO));
        dslStandardDataList1.add(createDtoObject("1024/256","Kbps/kbps", "SFR", "-", "ACA 1024","Surfer 512 Display", ApplicabilityEnum.NO));
        dslStandardDataList1.add(createDtoObject("512/128","Kbps/kbps", "SFR", "-", "ACA 512","Surfer 512 Display", ApplicabilityEnum.YES));
        dslStandardDataList1.add(createDtoObject("1024/256","Kbps/kbps", "SFR", "-", "Surfer 1024 Pro","Surfer 512 Display", ApplicabilityEnum.NORESPONSE));

        sacDto1.add(AccessTechnologyEnum.DSLSTANDARD.toString(), dslStandardDataList1);
        sacDto1.add(AccessTechnologyEnum.DSLPREMIUM.toString(), dslPrimiumDataList1);
        sacDto1.add(AccessTechnologyEnum.DSLPLUS.toString(), dslPlusDataList1);

        dto1.setSacXlAccessAndProductsMapDto(sacDto1);
        dto1.setCountry("India");
        dto1.setSiteName("site 1");
        dto1.setPhoneNo("22222222222");

        //Start 3rd row.

        SacXlRowDataModel dto2 = new SacXlRowDataModel();
        SacXlAccessTechAndProductsDetailsMap sacDto2 = new SacXlAccessTechAndProductsDetailsMap();

        List<SacXlSupplierProductDto> dslPrimiumDataList2 = new ArrayList<SacXlSupplierProductDto>();

        dslPrimiumDataList2.add(createDtoObject("640","Kbps/kbps", "SFR", "1", "REFLEX 05S","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPrimiumDataList2.add(createDtoObject("640","Kbps/kbps", "SFR", "2", "REFLEX 05S","Surfer 512 Display", ApplicabilityEnum.NO));
        dslPrimiumDataList2.add(createDtoObject("1280","Kbps/kbps", "SFR", "1", "REFLEX 1S 2P","Surfer 512 Display", ApplicabilityEnum.NO));
        dslPrimiumDataList2.add(createDtoObject("2048","Kbps/kbps", "SFR", "2", "REFLEX 2S 2P","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPrimiumDataList2.add(createDtoObject("512","Kbps/kbps", "Orange", "1", "SDSL 1P 500K garanti (0,5gS)","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPrimiumDataList2.add(createDtoObject("1024","Kbps/kbps", "Orange", "1", "SDSL 1P 1000K garanti (1gS)","Surfer 512 Display", ApplicabilityEnum.NO));

        List<SacXlSupplierProductDto> dslPlusDataList2 = new ArrayList<SacXlSupplierProductDto>();
        dslPlusDataList2.add(createDtoObject("576/160","Kbps/kbps", "SFR", "1", "REFLEX 05A","Surfer 512 Display", ApplicabilityEnum.YES));
        dslPlusDataList2.add(createDtoObject("1216/320","Kbps/kbps", "SFR", "1", "REFLEX 1A","Surfer 512 Display", ApplicabilityEnum.NO));


        List<SacXlSupplierProductDto> dslStandardDataList2 = new ArrayList<SacXlSupplierProductDto>();
        dslStandardDataList2.add(createDtoObject("512/128","Kbps/kbps", "SFR", "-", "Surfer 512","Surfer 512 Display", ApplicabilityEnum.NO));
        dslStandardDataList2.add(createDtoObject("512/128","Kbps/kbps", "SFR", "-", "ACA 512","ACA 512 Display", ApplicabilityEnum.YES));
        dslStandardDataList2.add(createDtoObject("1024/256","Kbps/kbps", "SFR", "-", "Surfer 1024 Pro","Surfer 1024 Pro Display", ApplicabilityEnum.NORESPONSE));
        dslStandardDataList2.add(createDtoObject("1024/256","Kbps/kbps", "SFR", "-", "ACA 1024","ACA 1024 Display", ApplicabilityEnum.NO));

        sacDto2.add(AccessTechnologyEnum.DSLSTANDARD.toString(), dslStandardDataList2);
        sacDto2.add(AccessTechnologyEnum.DSLPREMIUM.toString(), dslPrimiumDataList2);
        sacDto2.add(AccessTechnologyEnum.DSLPLUS.toString(), dslPlusDataList2);

        dto2.setSacXlAccessAndProductsMapDto(sacDto2);
        dto2.setCountry("India");
        dto2.setSiteName("site 3");
        dto2.setPhoneNo("333333333");


        List<SacXlRowDataModel> list = new ArrayList<SacXlRowDataModel>();
        list.add(dto);
        list.add(dto1);
        list.add(dto2);
        //list= getDataModel();
        writer.writeToNewExcel(list);

    }

    private static SacXlSupplierProductDto createDtoObject(String accSpd,String speedUnit, String supplier, String couperPairNo, String prodName,String prodDispName, ApplicabilityEnum applicability) {
        SacXlSupplierProductDto dto = new SacXlSupplierProductDto();
        dto.setAccessSpeed(accSpd);
        dto.setSupplierName(supplier);
        dto.setNoOfCopperPairs(couperPairNo);
        dto.setSupplierProductName(prodName);
        dto.setApplicability(applicability);
        dto.setSupplierProductDispName(prodDispName);
        dto.setAccessSpeedUnit(speedUnit);
        return dto;
    }

 /*  public List<SacXlRowDataModel> getDataModel(){
       AvailabilityCheckerResourceHandler handler=new AvailabilityCheckerResourceHandler(null,null,null,null);
       List<SacSiteDTO> siteDTOs=new ArrayList<SacSiteDTO>();
       List<SacSupplierProdAvailDTO> sacSupplierProdAvailDTOs=new ArrayList<SacSupplierProdAvailDTO>();
       List<SacSupplierProdAvailDTO> sacSupplierProdAvailDTOs2=new ArrayList<SacSupplierProdAvailDTO>();

       SacSupplierProdDetailDTO sacSupplierProdDetailDTO1=getSacSupplierProdDetailDTO("ACA 512","512K/128K","1","SFR", "No","ACA 512","ADSL");

       SacSupplierProdDetailDTO sacSupplierProdDetailDTO2=getSacSupplierProdDetailDTO("1024K/256K","51024K/256K","2","SFR", "Yes","Surfer 1024 Pro","ADSL");

       SacSupplierProdDetailDTO sacSupplierProdDetailDTO3=getSacSupplierProdDetailDTO("ACA 512","512K/128K","1","SFR", "No","ACA 512","SDSL");

       SacSupplierProdDetailDTO sacSupplierProdDetailDTO4=getSacSupplierProdDetailDTO("1024K/256K","1024K/256K","2","SFR", "Yes","Surfer 1024 Pro","SDSL");

       SacSupplierProdAvailDTO sacSupplierProdAvailDTO1=getSacSupplierProdAvailDTO(sacSupplierProdDetailDTO1.getSpacId(),"666",sacSupplierProdDetailDTO1);
       sacSupplierProdAvailDTOs.add(sacSupplierProdAvailDTO1);

       SacSupplierProdAvailDTO sacSupplierProdAvailDTO2=getSacSupplierProdAvailDTO(sacSupplierProdDetailDTO2.getSpacId(),"666",sacSupplierProdDetailDTO2);
       sacSupplierProdAvailDTOs.add(sacSupplierProdAvailDTO2);

       SacSupplierProdAvailDTO sacSupplierProdAvailDTO3 = getSacSupplierProdAvailDTO(sacSupplierProdDetailDTO3.getSpacId(),"666",sacSupplierProdDetailDTO3);
       sacSupplierProdAvailDTOs.add(sacSupplierProdAvailDTO3);
       SacSupplierProdAvailDTO sacSupplierProdAvailDTO4 = getSacSupplierProdAvailDTO(sacSupplierProdDetailDTO4.getSpacId(),"666",sacSupplierProdDetailDTO4);
       sacSupplierProdAvailDTOs.add(sacSupplierProdAvailDTO4);


       SacSupplierProdDetailDTO sacSupplierProdDetailDTO12=getSacSupplierProdDetailDTO("ACA 512","512K/128K","1","SFR", "Yes","ACA 512","ADSL");

       SacSupplierProdDetailDTO sacSupplierProdDetailDTO22=getSacSupplierProdDetailDTO("1024K/256K","51024K/256K","2","SFR", "No","Surfer 1024 Pro","ADSL");

       SacSupplierProdDetailDTO sacSupplierProdDetailDTO32=getSacSupplierProdDetailDTO("ACA 512","512K/128K","1","SFR", "Yes","ACA 512","SDSL");

       SacSupplierProdDetailDTO sacSupplierProdDetailDTO42=getSacSupplierProdDetailDTO("1024K/256K","1024K/256K","2","SFR", "No Response","Surfer 1024 Pro","SDSL");

       SacSupplierProdAvailDTO sacSupplierProdAvailDTO12=getSacSupplierProdAvailDTO(sacSupplierProdDetailDTO12.getSpacId(),"777",sacSupplierProdDetailDTO12);


       SacSupplierProdAvailDTO sacSupplierProdAvailDTO22=getSacSupplierProdAvailDTO(sacSupplierProdDetailDTO22.getSpacId(),"777",sacSupplierProdDetailDTO22);
       sacSupplierProdAvailDTOs2.add(sacSupplierProdAvailDTO22);
       sacSupplierProdAvailDTOs2.add(sacSupplierProdAvailDTO12);
       SacSupplierProdAvailDTO sacSupplierProdAvailDTO32 = getSacSupplierProdAvailDTO(sacSupplierProdDetailDTO32.getSpacId(),"777",sacSupplierProdDetailDTO32);
       sacSupplierProdAvailDTOs2.add(sacSupplierProdAvailDTO32);

       SacSupplierProdAvailDTO sacSupplierProdAvailDTO42 = getSacSupplierProdAvailDTO(sacSupplierProdDetailDTO42.getSpacId(),"777",sacSupplierProdDetailDTO42);
       sacSupplierProdAvailDTOs2.add(sacSupplierProdAvailDTO42);



       SacSiteDTO siteDTO1=new SacSiteDTO();
       siteDTO1.setCountryIsoCode("IN");
       siteDTO1.setCountryName("India");
       siteDTO1.setSiteId("666");
       siteDTO1.setSiteName("666");
       siteDTO1.setTelephoneNo("0123456789");
       siteDTO1.setSacSupplierProdAvailDTOs(sacSupplierProdAvailDTOs);

       SacSiteDTO siteDTO2=new SacSiteDTO();
       siteDTO2.setCountryIsoCode("IN");
       siteDTO2.setCountryName("India");
       siteDTO2.setSiteId("777");
       siteDTO2.setSiteName("777");
       siteDTO2.setTelephoneNo("0123456100");
       siteDTO2.setSacSupplierProdAvailDTOs(sacSupplierProdAvailDTOs2);

       siteDTOs.add(siteDTO1);
       siteDTOs.add(siteDTO2);

       return handler.convertToXlRowDataModel(siteDTOs);
    }
*/
    private  SacSupplierProdDetailDTO getSacSupplierProdDetailDTO(String soacId, String accessSpeed, String noOfCopperWeare, String supplierName, String applicability, String prodName, String accessType){
        SacSupplierProdDetailDTO sacSupplierProdDetailDTO4=new SacSupplierProdDetailDTO();
        sacSupplierProdDetailDTO4.setSpacId(soacId);
        sacSupplierProdDetailDTO4.setAccessSpeed(accessSpeed);
        sacSupplierProdDetailDTO4.setNoOfCopperPairs(noOfCopperWeare);
        sacSupplierProdDetailDTO4.setSupplierName(supplierName);
        sacSupplierProdDetailDTO4.setApplicability(applicability);
        sacSupplierProdDetailDTO4.setSupplierProductName(prodName);
        sacSupplierProdDetailDTO4.setAccessType(accessType);

        return sacSupplierProdDetailDTO4;
    }

   private SacSupplierProdAvailDTO getSacSupplierProdAvailDTO(String spacId, String siteId,SacSupplierProdDetailDTO sacSupplierProdDetailDTO ){
       SacSupplierProdAvailDTO sacSupplierProdAvailDTO1=new SacSupplierProdAvailDTO();
       sacSupplierProdAvailDTO1.setSpacId(spacId);
       sacSupplierProdAvailDTO1.setSiteId(siteId);
       sacSupplierProdAvailDTO1.setSacSupplierProdDetailDTO(sacSupplierProdDetailDTO);
       return sacSupplierProdAvailDTO1;
   }
}

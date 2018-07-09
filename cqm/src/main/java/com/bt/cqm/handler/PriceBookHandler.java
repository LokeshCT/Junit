package com.bt.cqm.handler;

import com.bt.cqm.dto.ChannelHierarchyDTO;
import com.bt.cqm.dto.PriceBookDTO;
import com.bt.cqm.repository.channelhierarchy.PriceBookRepository;
import com.bt.rsqe.customerinventory.client.resource.BfgPricebookResourceClient;
import com.bt.rsqe.customerinventory.dto.pricebook.PriceBookExtnDTO;
import com.bt.rsqe.expedio.product.PriceDetails;
import com.bt.rsqe.ppsr.client.PriceBookResource;
import com.bt.rsqe.ppsr.client.ProductResource;
import com.bt.rsqe.ppsr.client.dto.ProductDTO;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.web.rest.exception.RestException;
import com.google.common.base.Function;
import com.google.common.base.Predicate;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;

import static com.bt.cqm.utils.Utility.*;
import static com.google.common.collect.Iterables.*;
import static com.google.common.collect.Lists.*;
import static com.google.common.collect.Lists.transform;

/**
 * Created with IntelliJ IDEA.
 * User: 607520161
 * Date: 20/11/13
 * Time: 16:24
 * To change this template use File | Settings | File Templates.
 */

@Path("/cqm/pricebook")
@Produces(MediaType.APPLICATION_JSON)
@Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
public class PriceBookHandler {
    private PriceBookRepository priceBookRepository;
    private ProductResource productResource;
    private PriceBookResource priceBookResource;
    private com.bt.rsqe.expedio.product.ProductResource productExpedioResource;
    private ChannelHierarchyResource channelHierarchyResource;
    private BfgPricebookResourceClient bfgPricebookResourceClient;

    public PriceBookHandler(ProductResource productResource, PriceBookResource priceBookResource, com.bt.rsqe.expedio.product.ProductResource productExpedioResource, ChannelHierarchyResource channelHierarchyResource, BfgPricebookResourceClient bfgPricebookResourceClient) {
        this.productResource = productResource;
        this.priceBookResource = priceBookResource;
        this.productExpedioResource = productExpedioResource;
        this.channelHierarchyResource = channelHierarchyResource;
        this.bfgPricebookResourceClient = bfgPricebookResourceClient;
    }

    @GET
    @Path("getProductNames")
    public Response getProductNames(@QueryParam("salesChannelId") String salesChannelId) {

        if (AssertObject.anyEmpty(salesChannelId)) {
            return ResponseBuilder.badRequest().withEntity(buildGenericError("Mandatory Parameters are Missing")).build();
        }

        List<ProductDTO> products = productResource.getProductsLinkedTo(salesChannelId);
        if (products.isEmpty()) {
            return ResponseBuilder.notFound().build();
        }

        ListIterator li = products.listIterator();
        List<ProductDTO> productsWithoutDuplicates = new ArrayList<ProductDTO>(products.size());
        while (li.hasNext()) {
            ProductDTO tempProductDTO = (ProductDTO) li.next();
            if (!productsWithoutDuplicates.contains(tempProductDTO)) {
                productsWithoutDuplicates.add(tempProductDTO);
            }
        }
        List<PriceBookDTO> priceBookDTOs = newArrayList(transform(productsWithoutDuplicates, new Function<ProductDTO, PriceBookDTO>() {
            @Override
            public PriceBookDTO apply(final ProductDTO input) {
                return new PriceBookDTO(input.getCategoryName());
            }
        }));

        HashSet<PriceBookDTO> priceBookDTOHashSet = new HashSet<PriceBookDTO>();
        priceBookDTOHashSet.addAll(priceBookDTOs);
        List<PriceBookDTO> priceBookDTOList = new ArrayList<PriceBookDTO>();
        priceBookDTOList.addAll(priceBookDTOHashSet);

        GenericEntity<List<PriceBookDTO>> entity = new GenericEntity<List<PriceBookDTO>>(priceBookDTOList) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();

    }

    @GET
    @Path("getProductVersions")
    public Response getProductVersions(@QueryParam("salesChannelId") String salesChannelId, @QueryParam("customerId") String customerId,
                                       @QueryParam("productName") final String productName) {
        if (AssertObject.anyEmpty(salesChannelId, customerId, productName)) {
            return ResponseBuilder.badRequest().withEntity(buildGenericError("Invalid SalesChannel ID/Customer ID/Product Name")).build();
        }
        String versionDetail = null;
        //Get the Product Key based on the Channel Id and product Name
        List<ProductDTO> products = productResource.getProductsLinkedTo(salesChannelId);
        ProductDTO product = null;

        try {
            product = find(products, new Predicate<ProductDTO>() {
                @Override
                public boolean apply(ProductDTO input) {
                    return input.getCategoryName().equals(productName);
                }
            });
        } catch (Exception ex) {
            return Response.status(Response.Status.NOT_FOUND).entity(buildGenericError("No product exist by the name - " + productName)).build();
        }

        String ptpPriceBookVersion = "";
        String rrpPriceBookVersion = priceBookResource.getRRPPriceBookCategoryCode(product.getCategoryCode()).getVersion();

        ChannelHierarchyDTO channelHierarchyDTO = null;
        try {
            channelHierarchyDTO = channelHierarchyResource.loadChannelPartnerDetailsOfCustomer(customerId);
            if (channelHierarchyDTO == null) {
                ptpPriceBookVersion = "";
            }
        } catch (RestException rEx) {
            ptpPriceBookVersion = "";
        } catch (Exception e) {
            //return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
            ptpPriceBookVersion = "";
        }
        if (channelHierarchyDTO != null) {
            try {
                ptpPriceBookVersion = priceBookResource.getPTPPriceBook(product.getCategoryCode(), customerId, channelHierarchyDTO.getTradeLevel()).getVersion();
            } catch (Exception e) {  // In case the PTP Price Book is not found and other exceptions.
                ptpPriceBookVersion = "";
            }
        }
        PriceDetails priceDetails = new PriceDetails();
        priceDetails.setPbPTPVersion(ptpPriceBookVersion);
        priceDetails.setPbEUPVersion(rrpPriceBookVersion);
        priceDetails.setPbProductId(product.getProductScode());
        priceDetails.setPbProductName(product.getProductName());
        GenericEntity<PriceDetails> entity = new GenericEntity<PriceDetails>(priceDetails) {
        };

        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    @GET
    @Path("getPriceBookDetailsCustomerId")
    public Response getPriceBookDetailsOfCustomer(@QueryParam("customerId") String customerId) {

        if (AssertObject.isEmpty(customerId)) {
            return ResponseBuilder.badRequest().withEntity(buildGenericError("Invalid Customer ID.")).build();
        }

        List<PriceDetails> priceDetails = null;
        priceDetails = productExpedioResource.getPriceBookDetails(customerId);

        if (priceDetails != null && priceDetails.size() > 0) {
            Collections.sort(priceDetails);
            Set<PriceDetails> priceDetailsSet = new LinkedHashSet<PriceDetails>(priceDetails);
            priceDetails.clear();
            priceDetails.addAll(priceDetailsSet);
        }
        GenericEntity<List<PriceDetails>> entity = new GenericEntity<List<PriceDetails>>(priceDetails) {
        };
        return ResponseBuilder.anOKResponse().withEntity(entity).build();
    }

    @POST
    @Path("savePriceBook")
    public Response createPriceBook(com.bt.rsqe.expedio.pricebook.PriceBookDTO pricebook) {

        if (AssertObject.anyEmpty(pricebook) || AssertObject.anyEmpty(pricebook.getSalesChannelName(), pricebook.getCustomerId(), pricebook.getCustomerId(), pricebook.getCustomerName(), pricebook.getRrpVersion(), pricebook.getPtpVersion())) {
            return ResponseBuilder.badRequest().withEntity(buildGenericError("Invalid PriceBook / SalesChannel")).build();
        }

        String productScode = "NULL";
        String packageProductName = null;
        String categoryCode = null;
        String prodKey = null;


        List<ProductDTO> products = productResource.getProductsLinkedTo(pricebook.getSalesChannelName());
        ListIterator listIterator = products.listIterator();
        while (listIterator.hasNext()) {
            ProductDTO productDTO = (ProductDTO) listIterator.next();
            if (pricebook.getProductName().equals(productDTO.getCategoryName())) {
                prodKey = productDTO.getProdkey();
                productScode = productDTO.getProductScode();
                categoryCode = productDTO.getCategoryCode();
                break;
            }
        }
        try {
            packageProductName = priceBookResource.getCategoryCodePriceBook(prodKey).getPackageProductName();
        } catch (Exception e) {
        }
        if (null == packageProductName) {
            packageProductName = pricebook.getProductName().toUpperCase();
        }
        pricebook.setProductScode(productScode);
        pricebook.setPmfcategoryID(categoryCode);
        pricebook.setProductKey(prodKey);
        pricebook.setPackageProductname(packageProductName);

        boolean success = productExpedioResource.saveBookDetails(pricebook);

        if (success) {
            return ResponseBuilder.anOKResponse().build();
        } else {
            return ResponseBuilder.internalServerError().build();
        }


    }

    @POST
    @Path("updatePricebook")
    public Response updatePricebookToBfg(@HeaderParam("SM_USER") String userId, com.bt.rsqe.customerinventory.dto.pricebook.PriceBookDTO priceBookDTO) {

        if (AssertObject.anyEmpty(userId, priceBookDTO)) {
            return ResponseBuilder.badRequest().withEntity(buildGenericError("Invalid Data. UserId / priceBookDTO is empty")).build();
        }

        Long pbId = bfgPricebookResourceClient.updatePricebook(userId, priceBookDTO);
        PriceBookExtnDTO priceBookExtnDTO = priceBookDTO.getPriceBookExtension();
        if (pbId != null && priceBookExtnDTO != null) {
            priceBookExtnDTO.setPbId(pbId);
            bfgPricebookResourceClient.createPricebookExtn(userId, priceBookExtnDTO);
        }

        return ResponseBuilder.anOKResponse().build();
    }

    @GET
    @Path("getPricebookExtension")
    public Response getPriceBookExtension(@QueryParam("peID") String pricebookId) {
        if (AssertObject.anyEmpty(pricebookId)) {
           return ResponseBuilder.badRequest().withEntity(buildGenericError("Invalid Data. priceBookId is blank")).build();
       }
        PriceBookExtnDTO priceBookExtnDTO = null;

        try {
            priceBookExtnDTO = bfgPricebookResourceClient.getPricebookExtension(pricebookId);
        } catch (RestException ex) {
           throw ex;
        } catch (Exception ex) {
            ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }


        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<PriceBookExtnDTO>(priceBookExtnDTO) {
        }).build();
    }

    @GET
    @Path("getBfgPricebook")
    public Response getBfgPriceBook(@QueryParam("cusId") String custId) {
        if (AssertObject.anyEmpty(custId)) {
            return ResponseBuilder.badRequest().withEntity(buildGenericError("Invalid Data. custId is blank")).build();
        }
        com.bt.rsqe.customerinventory.dto.pricebook.PriceBookDTO priceBookDTO = null;

        try {
            priceBookDTO = bfgPricebookResourceClient.getPricebook(custId);
        } catch (RestException ex) {
            throw ex;
        } catch (Exception ex) {
            ResponseBuilder.internalServerError().withEntity(buildGenericError(ex.getMessage())).build();
        }


        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<com.bt.rsqe.customerinventory.dto.pricebook.PriceBookDTO>(priceBookDTO) {
        }).build();
    }

    @POST
    @Path("getPriceBookCodes")
    public Response gertPriceBookProductCodes(com.bt.rsqe.expedio.pricebook.PriceBookDTO pricebook) {

        if (AssertObject.anyEmpty(pricebook) || AssertObject.anyEmpty(pricebook.getSalesChannelName(), pricebook.getCustomerId(), pricebook.getCustomerId(), pricebook.getCustomerName(), pricebook.getRrpVersion(), pricebook.getPtpVersion())) {
            return ResponseBuilder.badRequest().withEntity(buildGenericError("Invalid PriceBook / SalesChannel")).build();
        }

        String productScode = "NULL";
        String packageProductName = null;
        String categoryCode = null;
        String prodKey = null;
        List<ProductDTO> products = productResource.getProductsLinkedTo(pricebook.getSalesChannelName());
        ListIterator listIterator = products.listIterator();
        while (listIterator.hasNext()) {
            ProductDTO productDTO = (ProductDTO) listIterator.next();
            if (pricebook.getProductName().equals(productDTO.getCategoryName())) {
                prodKey = productDTO.getProdkey();
                productScode = productDTO.getProductScode();
                categoryCode = productDTO.getCategoryCode();
                break;
            }
        }
        pricebook.setProductScode(productScode);
        pricebook.setPmfcategoryID(categoryCode);
        pricebook.setProductKey(prodKey);
        pricebook.setPackageProductname(packageProductName);
        return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<com.bt.rsqe.expedio.pricebook.PriceBookDTO>(pricebook) {
        }).build();
    }

}

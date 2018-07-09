package com.bt.nrm.handler;

import com.bt.nrm.repository.ProductTemplateRepository;
import com.bt.nrm.util.GeneralUtil;
import com.bt.pms.dto.ProductCategoryDTO;
import com.bt.pms.dto.TemplateDTO;
import com.bt.pms.resources.PMSResource;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Date;
import java.util.List;

@Path("/nrm/productTemplates")
public class ProductTemplateResourceHandler extends ViewFocusedResourceHandler {

    PMSResource pmsResource;
    private final ProductTemplateRepository productTemplateRepository;

    public ProductTemplateResourceHandler(PMSResource pmsResource, final ProductTemplateRepository productTemplateRepository) {
        super(new Presenter());
        this.pmsResource = pmsResource;
        this.productTemplateRepository  = productTemplateRepository;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getTemplateByTemplateCode")
    public Response getTemplateByTemplateId(@QueryParam("templateCode") String templateCode, @QueryParam("templateVersion") String templateVersion) {
        try {
            TemplateDTO template = pmsResource.getCompleteTemplateDetails(templateCode, templateVersion);
            return ResponseBuilder.anOKResponse().withEntity(new GenericEntity<TemplateDTO>(template) {}).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getProductsByUserId")
    public Response getProductsByUserId(@QueryParam("userId") String userEIN) {
        //ToDo Change below code and accommodate PMS service call to fetch product data if needed
        /*try {
            if(isNotNull(userEIN)){
                List<ProductMasterEntity> allProductEntities = productTemplateRepository.getProductsByUserId(userEIN);
                List<ProductDTO> allProductDTOs = newArrayList(transform(allProductEntities, new Function<ProductMasterEntity, ProductDTO>() {
                    @Override
                    public ProductDTO apply(ProductMasterEntity input) {
                        return input.toDTO(new ProductDTO());
                    }
                }));
                return ResponseBuilder.anOKResponse()
                                      .withEntity(new GenericEntity<List<ProductDTO>>(allProductDTOs) {})
                                      .build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (ProductNotFoundException e) {
            e.printStackTrace();
            return Response.status(Response.Status.NOT_FOUND).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }*/
        return null;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getAllProducts")
    public Response getAllProducts() {
        //ToDo Change below code and accommodate PMS service call to fetch product data if needed
        try {
            List<ProductCategoryDTO> productCategoryList = pmsResource.getAllTemplates();
            return ResponseBuilder.anOKResponse()
                                  .withEntity(new GenericEntity<List<ProductCategoryDTO>>(productCategoryList) {}).build();
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/getTemplatesByProductId")
    public Response getTemplatesByProductId(@QueryParam("productId") String productId) {
        //ToDo Change below code and accommodate PMS service call to fetch product data if needed
        /*try {
            if(isNotNull(productId)){
                List<TemplateMasterEntity> templates = productTemplateRepository.getTemplatesByProductId(productId);
                JsonObject templatesJson = new JsonObject();
                String templatesJsonStr = new GsonBuilder().create().toJson(templates);
                templatesJson.add("Templates" , new JsonPrimitive(templatesJsonStr));
                return Response.ok(templatesJson).build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }*/
        return null;
    }

    /*public List<ProductDTO> getAllProductDTOs() {
            List<ProductMasterEntity> allProductEntities = productTemplateRepository.getAllProducts();
            List<ProductDTO> allProductDTOs = newArrayList(transform(allProductEntities, new Function<ProductMasterEntity, ProductDTO>() {
                @Override
                public ProductDTO apply(ProductMasterEntity input) {
                    return input.toDTO(new ProductDTO());
                }
            }));
            return allProductDTOs;

    }

    public List<ProductDTO> getProductsDelta(List<ProductDTO> userProducts){  //This method compares the products sent as a parameter with super set of NRM products and returns the list of missing products
        List<ProductDTO> allProducts = getAllProductDTOs();
        List<ProductDTO> unassignedProducts = new ArrayList<ProductDTO>();

        for(ProductDTO product : allProducts){
            if(!userProducts.contains(product)){
                unassignedProducts.add(product);
            }
        }
        return unassignedProducts;

    }

    */

}

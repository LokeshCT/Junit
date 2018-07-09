package com.bt.nrm.handler;

import com.bt.nrm.dto.NRMUserDTO;
import com.bt.nrm.dto.UserGroupDTO;
import com.bt.nrm.dto.UserProductDTO;
import com.bt.nrm.repository.NRMUserManagementRepository;
import com.bt.nrm.repository.ProductTemplateRepository;
import com.bt.nrm.repository.entity.UserGroupConfigID;
import com.bt.nrm.repository.entity.UserGroupEntity;
import com.bt.nrm.repository.entity.UserProductEntity;
import com.bt.pms.dto.EvaluatorGroupDTO;
import com.bt.pms.dto.ProductCategoryDTO;
import com.bt.pms.resources.PMSResource;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.bt.usermanagement.dto.UserDTO;
import com.bt.usermanagement.dto.UserRoleDTO;
import com.bt.usermanagement.resources.UserResource;
import com.bt.usermanagement.util.UserManagementConstants;
import com.google.common.base.Function;

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
import java.util.HashMap;
import java.util.List;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;
import com.bt.nrm.util.GeneralUtil;

@Path("/nrm/user")
public class NRMUserManagementHandler extends ViewFocusedResourceHandler {

    private final NRMUserManagementRepository nrmUserManagementRepository;
    private final ProductTemplateRepository productTemplateRepository;
    private UserResource userResource;
    PMSResource pmsResource;

    public NRMUserManagementHandler(UserResource userResource, final NRMUserManagementRepository nrmUserManagementRepository, ProductTemplateRepository productTemplateRepository,PMSResource pmsResource) {
        super(new Presenter());
        this.userResource = userResource;
        this.nrmUserManagementRepository  = nrmUserManagementRepository;
        this.productTemplateRepository = productTemplateRepository;
        this.pmsResource = pmsResource;
    }

    @GET
    @Path("/getNrmUserByUserId")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getNrmUserByUserId(@HeaderParam("SM_USER") String nrmUserEIN) { //Get UserInfo at login
        try {
            if(isNotNull(nrmUserEIN)){
                NRMUserDTO nrmUser = getNRMUserByUserId(nrmUserEIN);
                if(isNotNull(nrmUser)){
                    //Get Products Associated to User
                    List<UserProductDTO> userProducts = getNRMUserProductDTOs(nrmUserEIN);
                    if(isNotNull(userProducts)){
                        nrmUser.setProducts(userProducts);
                    }

                    //Get Groups Associated to User
                    List<UserGroupDTO> userGroups = getNRMUserGroupDTOs(nrmUserEIN);
                    if(isNotNull(userGroups)){
                        nrmUser.setGroups(userGroups);
                    }

                    return ResponseBuilder.anOKResponse()
                                            .withEntity(new GenericEntity<NRMUserDTO>(nrmUser) {})
                                            .build();
                    }
                }
            return Response.status(Response.Status.BAD_REQUEST).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Path("/getUserByEINOrName")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserByEINOrName(@QueryParam("EINOrName") String EINOrName) { //This method accepts EIN/FirstName or LastName and returns basic info of user(s)
        try {
            if(isNotNull(EINOrName)){
                List<UserDTO> userDTOList = userResource.getUserByEINOrName(EINOrName);
                List<NRMUserDTO> nrmUserDTOList = new ArrayList<NRMUserDTO>();
                for(UserDTO userDTO : userDTOList){
                    nrmUserDTOList.add(NRMUserDTO.getNRMDTOFromUserDTO(userDTO));
                }
                return ResponseBuilder.anOKResponse()
                                      .withEntity(new GenericEntity<List<NRMUserDTO>>(nrmUserDTOList) {})
                                      .build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Path("/getUserManagementData")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserManagementData(@QueryParam("userId") String userId) { //This method accepts EIN/FirstName or LastName and returns basic info of user(s)
        try {
            if(isNotNull(userId)){
                List<EvaluatorGroupDTO> allEvaluatorGroups = getAllEvaluatorGroups();
                List<ProductCategoryDTO>  allProductCategories = getAllProductCategories();
                NRMUserDTO nrmUser = getNRMUserByUserId(userId);
                if(isNotNull(nrmUser)){
                    //Get Products Associated to User
                    List<UserProductDTO> userProducts = getNRMUserProductDTOs(userId);
                    if(isNotNull(userProducts)){
                        nrmUser.setProducts(setUserProductDetails(userProducts,allProductCategories));
                    }
                    //Get Groups Associated to User
                    List<UserGroupDTO> userGroups = getNRMUserGroupDTOs(userId);
                    if(isNotNull(userGroups)){
                        nrmUser.setGroups(setUserGroupDetails(userGroups, allEvaluatorGroups,allProductCategories));
                    }

                    HashMap dataObject = new HashMap();
                    dataObject.put("nrmUser", nrmUser);
                    dataObject.put("allProductCategory", allProductCategories);
                    dataObject.put("allEvaluatorGroups", allEvaluatorGroups);
                    dataObject.put("unassignedProducts", getProductsDelta(nrmUser.getProducts()));
                    dataObject.put("allRoles", userResource.getAllRolesByRoleGroup(UserManagementConstants.roleGroupConstants.get("NRM_ROLE_GROUP_ID")));
                    return ResponseBuilder.anOKResponse()
                                            .withEntity(new GenericEntity<HashMap>(dataObject){})
                                            .build();
                }
            }
            return Response.status(Response.Status.BAD_REQUEST).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }



    @POST
    @Path("/addProductsToUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addProductsToUser(List<UserProductDTO> userProductList,@QueryParam("userId") String userId){
        try {
            if(isNotNull(userProductList)){
                boolean returnValue =  nrmUserManagementRepository.addProductsToUser(userProductList,userId);
                if(returnValue){
                    return Response.status(Response.Status.OK).build();
                }else {
                    return Response.status(Response.Status.NOT_FOUND).build();
                }
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @POST
    @Path("/addGroupToUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addGroupToUser(UserGroupDTO userGroupDTO) {
        try {
            if(isNotNull(userGroupDTO) && isNotNull(userGroupDTO.getProduct()) && isNotNull(userGroupDTO.getGroup())){
                UserGroupEntity userGroupEntity = new UserGroupEntity(new UserGroupConfigID(userGroupDTO.getProduct().getProductCategoryCode(), userGroupDTO.getUserId(), userGroupDTO.getGroup().getEvaluatorGroupId()), GeneralUtil.getCurrentTimeStamp(),
                                                                      userGroupDTO.getCreatedUser());
                nrmUserManagementRepository.addGroupToUser(userGroupEntity);
                return Response.status(Response.Status.OK).build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }


    @POST
    @Path("/deleteGroupFromUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteGroupFromUser(UserGroupDTO userGroupDTO) {
        //ToDo Change below code and accommodate PMS service call to fetch product data if needed
        try {
            if(isNotNull(userGroupDTO) && isNotNull(userGroupDTO.getProduct()) && isNotNull(userGroupDTO.getGroup())){
                UserGroupEntity userGroupEntity = new UserGroupEntity(new UserGroupConfigID(userGroupDTO.getProduct().getProductCategoryCode(), userGroupDTO.getUserId(), userGroupDTO.getGroup().getEvaluatorGroupId()), GeneralUtil.getCurrentTimeStamp(),
                                                                      userGroupDTO.getCreatedUser());
                nrmUserManagementRepository.deleteGroupFromUser(userGroupEntity);
                return Response.status(Response.Status.OK).build();
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }

    }

    @POST
    @Path("/deleteRoleFromUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response deleteRoleFromUser(UserRoleDTO userRoleDTO) {
        try {
            if(isNotNull(userRoleDTO)){
                Boolean result = userResource.deleteRoleFromUser(userRoleDTO);
                if (result) {
                    return Response.status(Response.Status.OK).build();
                } else {
                    return Response.status(Response.Status.EXPECTATION_FAILED).build();
                }
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @POST
    @Path("/addRoleToUser")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response addRoleToUser(UserRoleDTO userRoleDTO) {
        try {
            if(isNotNull(userRoleDTO)){
                Boolean result = userResource.addRoleToUser(userRoleDTO);
                if (result) {
                    return Response.status(Response.Status.OK).build();
                } else {
                    return Response.status(Response.Status.EXPECTATION_FAILED).build();
                }
            }else{
                return Response.status(Response.Status.BAD_REQUEST).build();
            }
        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    @GET
    @Path("/getUserStats")
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
    public Response getUserStats(@QueryParam("userId")String userId) {
        try {
            if(isNotNull(userId)){
                HashMap stats = new HashMap();
                stats.put("requestsCreated", "123");
                stats.put("signIns", "85");
                stats.put("signOffs", "110");
                return ResponseBuilder.anOKResponse()
                                        .withEntity(new GenericEntity<HashMap>(stats){})
                                        .build();
            }
            return Response.status(Response.Status.BAD_REQUEST).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.EXPECTATION_FAILED).build();
        }
    }

    public NRMUserDTO getNRMUserByUserId(String userId){
        if(isNotNull(userId)){
            UserDTO userDTO = userResource.getUserByUserId(userId);
            return NRMUserDTO.getNRMDTOFromUserDTO(userDTO);
        }
        return null;
    }

    public List<UserProductDTO> getNRMUserProductDTOs(String userId){
        if(isNotNull(userId)){
            List<UserProductEntity> userProductEntities = nrmUserManagementRepository.getNRMUserProductDTOs(userId);
            return convertUserProductEntityToDTO(userProductEntities);
        }
        return null;
    }

    public List<UserGroupDTO> getNRMUserGroupDTOs(String userId){
        if(isNotNull(userId)){
            List<UserGroupEntity> userGroupEntities = nrmUserManagementRepository.getGroupsFromUserId(userId);
            List<UserGroupDTO> userGroupDTOs = newArrayList(transform(userGroupEntities, new Function<UserGroupEntity, UserGroupDTO>() {
                @Override
                public UserGroupDTO apply(UserGroupEntity input) {
                    return input.toDTO(new UserGroupDTO());
                }
            }));
            return userGroupDTOs;
        }
        return null;
    }

    public List<EvaluatorGroupDTO> getAllEvaluatorGroups() {
        return pmsResource.getAllEvaluators();
    }

    private List<ProductCategoryDTO> getAllProductCategories(){
        return pmsResource.getAllProductCategories();
    }

    //This method compares the products sent as a parameter with super set of NRM products and returns the list of missing products
    private List<ProductCategoryDTO> getProductsDelta(List<UserProductDTO> userProducts){
        List<ProductCategoryDTO> allProducts = getAllProductCategories();
        List<ProductCategoryDTO> unassignedProducts = new ArrayList<ProductCategoryDTO>();

        for(ProductCategoryDTO product : allProducts){
            if(!allProductContainsUserProduct(product,userProducts)){
                unassignedProducts.add(product);
            }
        }
        return unassignedProducts;

    }

    private boolean allProductContainsUserProduct(ProductCategoryDTO productCategoryDTO,List<UserProductDTO> userProducts){
        boolean userProductContainsProduct = false;
        for(UserProductDTO userProductDTO:userProducts){
            if(productCategoryDTO.getProductCategoryCode().equals(userProductDTO.getProduct().getProductCategoryCode())){
                userProductContainsProduct= true;
                break;
            }
            else{
                userProductContainsProduct= false;
            }
        }
        return userProductContainsProduct;
    }

    private List<UserGroupDTO> setUserGroupDetails(List<UserGroupDTO> userGroups,List<EvaluatorGroupDTO> allEvaluatorGroups,List<ProductCategoryDTO>  allProductCategories){
        for(int i=0;i<userGroups.size();i++) {
            for(int j=0;j <allEvaluatorGroups.size();j++){
                 if(userGroups.get(i).getGroup().getEvaluatorGroupId().equals(allEvaluatorGroups.get(j).getEvaluatorGroupId())){
                     userGroups.get(i).setGroup(allEvaluatorGroups.get(j));
                 }
            }
            for(int k=0;k<allProductCategories.size();k++){
                if(userGroups.get(i).getProduct().getProductCategoryCode().equals(allProductCategories.get(k).getProductCategoryCode())){
                    userGroups.get(i).setProduct(allProductCategories.get(k));
                }
            }
        }
        return userGroups;
    }

    private List<UserProductDTO> setUserProductDetails(List<UserProductDTO> userProducts, List<ProductCategoryDTO> allProductCategories){
        for(int i=0;i<userProducts.size();i++) {
            for(int j=0;j <allProductCategories.size();j++){
                if(userProducts.get(i).getProduct().getProductCategoryCode().equals(allProductCategories.get(j).getProductCategoryCode())){
                    userProducts.get(i).setProduct(allProductCategories.get(j));
                }
            }
        }
        return userProducts;
    }

    private List<UserProductDTO> convertUserProductEntityToDTO(List<UserProductEntity> userProductEntities){
        List<UserProductDTO> userProductDTOs = new ArrayList<UserProductDTO>();
        for(UserProductEntity userProductEntity:userProductEntities){
            userProductDTOs.add(userProductEntity.toNewDTO());
        }
        return userProductDTOs;
    }
}




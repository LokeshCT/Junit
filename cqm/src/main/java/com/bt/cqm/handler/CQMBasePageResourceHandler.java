package com.bt.cqm.handler;

import com.bt.cqm.dto.UserType;
import com.bt.cqm.dto.user.SalesChannelDTO;
import com.bt.cqm.ldap.LDAPConstants;
import com.bt.cqm.ldap.LdapRepository;
import com.bt.cqm.ldap.SearchBTDirectoryHandler;
import com.bt.cqm.ldap.model.LdapSearchModel;
import com.bt.cqm.model.SalesUserDTO;
import com.bt.cqm.model.UserRoleDTO;
import com.bt.cqm.repository.user.RagConfigurationEntity;
import com.bt.cqm.repository.user.UserEntity;
import com.bt.cqm.repository.user.UserManagementRepository;
import com.bt.cqm.repository.user.UserRoleConfigEntity;
import com.bt.cqm.repository.user.UserSalesChannelEntity;
import com.bt.cqm.utils.Constants;
import com.bt.cqm.web.WebUtils;
import com.bt.rsqe.expedio.usermanagement.SubGroup;
import com.bt.rsqe.expedio.usermanagement.UserManagementResource;
import com.bt.rsqe.rest.ResponseBuilder;
import com.bt.rsqe.utils.AssertObject;
import com.bt.rsqe.utils.Lists;
import com.bt.rsqe.web.Presenter;
import com.bt.rsqe.web.ViewFocusedResourceHandler;
import com.google.common.base.Function;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import org.codehaus.jettison.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import static com.bt.rsqe.utils.AssertObject.*;
import static com.google.common.collect.Lists.*;


@Path("/cqm")
public class CQMBasePageResourceHandler extends ViewFocusedResourceHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(CQMBasePageResourceHandler.class);

    private final UserManagementRepository userRepository;
    private TabBuilder tabBuilder;
    private TabBuilder dslTabBuilder;
    private Gson gson;
    private LdapRepository ldapRepository;
    public static HashMap colorCodeHashMap;
    private UserManagementResource userManagementResource;

    public CQMBasePageResourceHandler(final UserManagementRepository userRepository, TabBuilder tabBuilder, LdapRepository ldapRepository,UserManagementResource userManagementResource) {
        super(new Presenter());
        this.userRepository = userRepository;
        this.tabBuilder = tabBuilder;
        this.ldapRepository = ldapRepository;
        this.gson = new Gson();
        this.userManagementResource=userManagementResource;
    }

    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response getCqmBasePage() throws JSONException {
        String page = new Presenter().render(view("cqmBasePage.ftl").withContext("urlConfig", UrlConfiguration.build()).withContext("customerCreationResponse", CustomerCreationResponse.toJson()));
        return WebUtils.responseOk(page);
    }

    @GET
    @Path("/dashboard")
    @Produces(MediaType.TEXT_HTML)
    public Response getDashboardPage() throws JSONException {
        String page = new Presenter().render(view("dashboard.ftl").withContext("urlConfig", UrlConfiguration.build()));
        return WebUtils.responseOk(page);
    }


    @GET
    @Path("/salesUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getSalesUser(@HeaderParam("SM_USER") String salesUserEIN, @Context HttpHeaders headers) {

        if (AssertObject.anyEmpty(salesUserEIN)) {
            LOGGER.warn("CQM :: Attempted to load empty user id.");
            return Response.status(Response.Status.BAD_REQUEST).entity("Empty User ID !!").build();
        }
        String userType = null;

        try {
            String salesUserAddOnString = "IUSER\\";
            if (null != salesUserEIN && salesUserEIN.contains(salesUserAddOnString)) {
                salesUserEIN = salesUserEIN.substring(salesUserAddOnString.length());
            }
        } catch (Exception e) {

        }
        getColorCodeHashMap();
        UserEntity user = userRepository.findUserByUserId(salesUserEIN);
        if (isNotNull(user) && user.isActive()) {
            LOGGER.info("CQM :: User [" + salesUserEIN + "] loaded ..");
            List<UserSalesChannelEntity> salesChannelEntities = userRepository.getAssociatedSalesChannel(salesUserEIN);
            List<SalesChannelDTO> salesChannels = newArrayList(transform(salesChannelEntities, new Function<UserSalesChannelEntity, SalesChannelDTO>() {
                @Override
                public SalesChannelDTO apply(UserSalesChannelEntity input) {
                    return new SalesChannelDTO(input.getSalesChannel(), input.getSalesChannel());
                }
            }));

            List<UserRoleConfigEntity> roleConfigs = userRepository.getUserRoleConfig(salesUserEIN);
            List<UserRoleDTO> userRoles = newArrayList(transform(roleConfigs, new Function<UserRoleConfigEntity, UserRoleDTO>() {
                @Override
                public UserRoleDTO apply(UserRoleConfigEntity input) {
                    return new UserRoleDTO(input.getId().getRole().getRoleId(), input.getId().getRole().getRoleName());
                }
            }));

            LdapSearchModel ldapSearchModel = searchBTDirectory(user.getUserId());
            String boatId = "";
            String mailId = "";

            if (ldapSearchModel != null) {
                boatId = ldapSearchModel.getBoatId() != null ? ldapSearchModel.getBoatId().toLowerCase() : null;
                mailId = ldapSearchModel.getMailId();
                LOGGER.warn("Unable to Fetch LDAP detail from BT Directory for User :" + user.getUserId());
            } else {
                mailId = user.getEmailId();
                boatId = user.getUserId();
            }


            if (isGSPLogin(headers)) {
                userType = Constants.INDIRECT_USER_TYPE;
                LOGGER.info("Tag User as Indirect.");
            } else {
                LOGGER.info("User Type selected from User Profile.");
                userType = isNotNull(user.getUserType()) ? user.getUserType().getRoleTypeName() : null;
            }
            //Code for Checking SubgroupUser
            boolean subGroupUser=false;
            String[] userSubGroups=null;
            try {
                if (ldapSearchModel != null && ldapSearchModel.getBoatId() != null) {
                    SubGroup subGroup = userManagementResource.getUserSubGroups(ldapSearchModel.getBoatId().toLowerCase());
                    if (subGroup != null && subGroup.getSubGroupList() != null) {

                        subGroupUser = true;
                        userSubGroups = subGroup.getSubGroupList().split(",");
                        if(userSubGroups!=null && userSubGroups.length==1 && userSubGroups[0].equals("ALL"))
                        {
                            subGroupUser=false;
                            //userSubGroups=null;
                        }

                    }

                }

            } catch (Exception e) {

            }
            //Code for Checking SubgroupUser

            SalesUserDTO salesUser = new SalesUserDTO(salesUserEIN,
                                                      boatId,
                                                      user.getUserName(),
                                                      mailId,
                                                      userRoles,
                                                      salesChannels,
                                                      userType,
                                                      subGroupUser);
            salesUser.setUserSubGroups(userSubGroups);

            return ResponseBuilder.anOKResponse().withEntity(salesUser).build();
        } else {
            LOGGER.warn("CQM :: Inactive User [" + salesUserEIN + "] attempted to login into application !!");
            return ResponseBuilder.notFound().withEntity(String.format("User %s doesn't exists or inactive.", salesUserEIN)).build();
        }
    }


    private LdapSearchModel searchBTDirectory(String ein) {
        Map<String, String> args = new HashMap<String, String>();
        args.put("ein", ein);

        List<LdapSearchModel> resultList = new ArrayList<LdapSearchModel>();

        try {
            resultList = ldapRepository.search(args);
        } catch (Exception e) {
            LOGGER.warn("Error while searching user in BT directory.", e);
        }

        LdapSearchModel ldapSearchResult = null;
        if (!Lists.isNullOrEmpty(resultList)) {
            ldapSearchResult = resultList.get(0);
        }
        return ldapSearchResult;
    }

    private boolean isGSPLogin(HttpHeaders headers) {
        //Map<String, Cookie> cookies = headers != null ? headers.getCookies() : null;
        List<String> hosts = headers != null ? headers.getRequestHeader("Host") : null;

        /* if (cookies != null) {
            LOGGER.info("Header Cookies :" + cookies);
        } else {
            LOGGER.info("No Header Cookies available !!");
        }

        String gspVal = getCookieValue(Constants.GSP_COOKIE_KEY, cookies);

        if (gspVal!=null && ("true".equalsIgnoreCase(gspVal) || gspVal.contains("true"))) {
            LOGGER.debug("Is a GS Portal Access !!");
            return true;
        } else {
            return false;
        }*/
        if (hosts != null) {
            LOGGER.info("Hosts :" + hosts);
            for (String aHost : hosts) {

                if (aHost != null && aHost.contains(Constants.GSP_HOST_NAME)) {
                    LOGGER.info("This is a GSP Access !!");
                    return true;
                }
            }
        }
        LOGGER.info("This is NOT a GSP Access !!");
        return false;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("tabs")
    public Response getTabs(@HeaderParam("SM_USER") String userId, @Context HttpHeaders headers, @QueryParam("appID") String appId) {
        JsonArray jsonArray = new JsonArray();
        Long userType = null;
        UserEntity user = userRepository.findUserByUserId(userId);
        if (AssertObject.isEmpty(appId) || ("CustomerConfiguration".equalsIgnoreCase(appId))) {
            if (isGSPLogin(headers)) {
                userType = Constants.INDIRECT_USER_TYPE_ID;
            } else {
                userType = isNotNull(user.getUserType()) ? user.getUserType().getRoleTypeId() : null;
            }

            List<UserRoleConfigEntity> roleConfigs = userRepository.getUserRoleConfig(userId);
            List<UserRoleDTO> userRoles = newArrayList(transform(roleConfigs, new Function<UserRoleConfigEntity, UserRoleDTO>() {
                @Override
                public UserRoleDTO apply(UserRoleConfigEntity input) {
                    return new UserRoleDTO(input.getId().getRole().getRoleId(), input.getId().getRole().getRoleName());
                }
            }));
            boolean hasSuperUser = false;

            for (UserRoleDTO aRole : userRoles) {
                if (aRole.getRoleName().equalsIgnoreCase("Super User")) {
                    hasSuperUser = true;
                    break;
                }
            }

            for (Tab tab : tabBuilder.build()) {
                JsonObject jsonObject = tab.asJson(userRoles.get(0).getRoleId(), UserType.findUserType(userType), hasSuperUser);
                if (jsonObject != null) {
                    jsonArray.add(jsonObject);
                }
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("tabs", jsonArray);

            return Response.ok(gson.toJson(jsonObject)).build();
        } else if ("DslChecker".equalsIgnoreCase(appId)) {
            if (dslTabBuilder == null) {
                dslTabBuilder = new TabBuilder("dsl-check-web-ui-config");
            }

            for (Tab tab : dslTabBuilder.build()) {
                JsonObject jsonObject = tab.asJson();
                if (jsonObject != null) {
                    jsonArray.add(jsonObject);
                }
            }

            JsonObject jsonObject = new JsonObject();
            jsonObject.add("tabs", jsonArray);

            return Response.ok(gson.toJson(jsonObject)).build();
        } else {
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity("No Application by the id :" + appId).build();
        }
    }

    @GET
    @Path("logout")
    @Produces(MediaType.TEXT_PLAIN)
    public Response logout(@QueryParam("boatId") String boatId) {
        LOGGER.info("Logging out user: " + boatId);
        LOGGER.info("Logged out user: " + boatId);
        return ResponseBuilder.anOKResponse().withEntity("You have successfully logged out!").build();
    }

    private void getColorCodeHashMap() {
        try {
            List<RagConfigurationEntity> ragConfigurationEntityList = userRepository.getColorCodeDetails();
            if (isNotNull(ragConfigurationEntityList) && ragConfigurationEntityList.size() >= 0) {
                colorCodeHashMap = new HashMap();
                ListIterator listIterator = ragConfigurationEntityList.listIterator();
                while (listIterator.hasNext()) {
                    RagConfigurationEntity ragConfigurationEntity = (RagConfigurationEntity) listIterator.next();
                    colorCodeHashMap.put(ragConfigurationEntity.getMpcCode().charAt(0) + ragConfigurationEntity.getStateCode() + "-" + ragConfigurationEntity.getFailLevel(), ragConfigurationEntity.getRagCode());
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String getCookieValue(String key, Map<String, Cookie> cookies) {
        if (cookies != null && cookies.size() > 0) {
            Cookie gsCookie = cookies.get(key);
            if (gsCookie != null) {
                LOGGER.info("Cookie Value :" + gsCookie.getValue());
                LOGGER.info("Cookie Name :" + gsCookie.getName());
                LOGGER.info("Cookie Path :" + gsCookie.getPath());
                LOGGER.info("Cookie Version :" + gsCookie.getVersion());
                LOGGER.info("Cookie Domain :" + gsCookie.getDomain());
                return gsCookie.getValue();
            }
        }

        return null;
    }

    private LdapSearchModel searchBTDirectoryWithEIN(String ein) {
        Map<String, String> args = new HashMap<String, String>();
        args.put(LDAPConstants.EIN, ein);
        List<LdapSearchModel> resultList = new SearchBTDirectoryHandler().searchBTDirectory(args);
        LdapSearchModel ldapSearchResult = null;
        if (resultList != null && !resultList.isEmpty()) {
            ldapSearchResult = resultList.get(0);
        }
        return ldapSearchResult;
    }

}

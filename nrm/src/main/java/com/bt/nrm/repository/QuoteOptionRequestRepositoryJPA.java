package com.bt.nrm.repository;

import com.bt.nrm.repository.entity.QuoteEntity;
import com.bt.nrm.repository.entity.RequestEntity;
import com.bt.nrm.repository.entity.RequestEvaluatorEntity;
import com.bt.nrm.repository.entity.UserProductEntity;
import com.bt.nrm.util.Constants;
import com.bt.nrm.util.GeneralUtil;
import com.bt.rsqe.EmailService;
import com.bt.rsqe.persistence.PersistenceManager;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static com.bt.rsqe.utils.AssertObject.*;

public class QuoteOptionRequestRepositoryJPA implements QuoteOptionRequestRepository {

    private final PersistenceManager persistenceManager;
    private final ProductTemplateRepository nrmRepository;


    public QuoteOptionRequestRepositoryJPA(PersistenceManager persistenceManager, ProductTemplateRepository nrmRepository) {
        this.persistenceManager = persistenceManager;
        this.nrmRepository = nrmRepository;
    }

    /*
        Quote
     */

    @Override
    public List<QuoteEntity> getAllQuoteOptions(){
        List<QuoteEntity> quoteEntityList = persistenceManager.query(QuoteEntity.class,"from QuoteEntity u ");
        return quoteEntityList;
    }

    @Override
    public List<RequestEntity> getAllRequestsByQuoteId(String quoteOptionId){
        List<RequestEntity> requestEntityList = persistenceManager.query(RequestEntity.class,"from RequestEntity re where re.quote.quoteMasterId = (?0)",quoteOptionId);
        return requestEntityList;
    }

    @Override
    public QuoteEntity getQuoteByQuoteOptionId(String quoteOptionId){
        try{
            if(isNotNull(quoteOptionId)){
                return persistenceManager.entityManager().createQuery("from QuoteEntity WHERE QUOTE_OPTION_ID = :quoteOptionId ", QuoteEntity.class).setParameter("quoteOptionId", quoteOptionId).getSingleResult();
            }
            return null;
        }
        catch(NoResultException noResultException){
            return null;
        }
        catch(Exception exception){
            exception.printStackTrace();
            return null;
        }
    }

    /*
        Requests
     */

    @Override
    public String getRequestIdFromSequence(){
        return persistenceManager.entityManager().createNativeQuery("SELECT REQUEST_MASTER_SEQUENCE.NEXTVAL FROM DUAL").getSingleResult().toString();
    }

    @Override
    public RequestEntity createRequest(RequestEntity requestEntity) {
        persistenceManager.entityManager().persist(requestEntity);
        return requestEntity;
    }

    @Override
    public RequestEntity getRequestsByRequestId(String requestId) {
        if(isNotNull(requestId)){
            return persistenceManager.entityManager().find(RequestEntity.class, requestId);
        }
        return null;
    }

    @Override
    public List<RequestEntity> getRequestsByUserId(String userId) {
        if(isNotNull(userId)){
            List<UserProductEntity> userProductConfigs = persistenceManager.query(UserProductEntity.class,"from UserProductEntity u where u.id.userId=?0",userId);
            Set<String> productIds = new HashSet<String>();
            for(UserProductEntity userProductConfigEntity : userProductConfigs){
                productIds.add(userProductConfigEntity.getId().getProductCategoryCode());
            }
            List<RequestEntity> requests = new ArrayList<RequestEntity>();
            if(productIds.size() > 0) {
                requests = persistenceManager.query(RequestEntity.class, "from RequestEntity reqEnt where reqEnt.productCategoryCode in (?0)", productIds);
            }
            return requests;
        }
        return null;
    }

    @Override
    public List<RequestEntity> getRequestsByUserIdAndStates(String userId, String requestStates) {
        if(isNotNull(userId) && isNotNull(requestStates)){
            List<UserProductEntity> userProductConfigs = persistenceManager.query(UserProductEntity.class,"from UserProductEntity u where u.id.userId=?0",userId);
            Set<String> productIds = new HashSet<String>();

            for(UserProductEntity userProductConfigEntity : userProductConfigs){
                productIds.add(userProductConfigEntity.getId().getProductCategoryCode());
            }
            List<RequestEntity> requests = new ArrayList<RequestEntity>();
            Set<String> states = new HashSet<String>();
            for(String state: requestStates.split(",")){
                if(isNotNull(state)){
                    if(state.equalsIgnoreCase(Constants.requestStateConstants.get("all"))){
                        Iterator reqStateItr = Constants.requestStateConstants.keySet().iterator();
                        while(reqStateItr.hasNext()){
                            states.add(Constants.requestStateConstants.get(reqStateItr.next()));
                        }
                    }
                    if(state.equalsIgnoreCase(Constants.requestStateConstants.get("allAgentsHaveFinishedWork"))){
                        requests.addAll(getRequestsWhereAllAgentsHaveFinishedWork(userId, productIds));
                    }
                    if(state.equalsIgnoreCase(Constants.requestStateConstants.get("noAgents"))){
                        requests.addAll(getRequestsWithoutEvaluators(userId, productIds));
                    }
                    states.add(state);
                }
            }
            if(productIds.size() > 0 && states.size()>0) {
                requests.addAll(persistenceManager.query(RequestEntity.class, "from RequestEntity reqEnt where reqEnt.state in (?0) and reqEnt.productCategoryCode in (?1)", states, productIds));
            }
            return requests;
        }
        return null;
    }

    public List<RequestEntity> getRequestsWithoutEvaluators(String userId, Set<String> productIds) {
        List<RequestEntity> requests = new ArrayList<RequestEntity>();
        if(productIds.size() > 0) {
            requests = persistenceManager.query(RequestEntity.class, "from RequestEntity reqEnt where reqEnt.requestGroups is empty and reqEnt.productCategoryCode in (?0)", productIds);
        }
        return requests;
    }

    public List<RequestEntity> getRequestsWhereAllAgentsHaveFinishedWork(String userId, Set<String> productIds) {
        List<RequestEntity> requests = new ArrayList<RequestEntity>();
        if(productIds.size() > 0) {
            requests = persistenceManager.query(RequestEntity.class,
                                                "from RequestEntity reqEnt where " +
                                                "exists(select reqGroup from RequestEvaluatorEntity reqGroup where reqGroup.requestEntity.requestId = reqEnt.requestId and reqGroup.state = '" + Constants.requestStateConstants.get("closed") + "') " +
                                                "and reqEnt.state = '" + Constants.requestStateConstants.get("signedIn") + "' and reqEnt.productCategoryCode in (?0)", productIds);
        }
        return requests;
    }

    @Override
    public int saveRequestComments(String modifiedBy, String requestId, String comments) {
        CriteriaBuilder cb = persistenceManager.entityManager().getCriteriaBuilder();
        CriteriaUpdate<RequestEntity> requestEntityCriteriaUpdate = cb.createCriteriaUpdate(RequestEntity.class);
        Root<RequestEntity> root = requestEntityCriteriaUpdate.from(RequestEntity.class);
        // update properties
        requestEntityCriteriaUpdate.set(root.get("comments"), comments);
        requestEntityCriteriaUpdate.set(root.get("modifiedUser"), modifiedBy);
        requestEntityCriteriaUpdate.set(root.get("modifiedDate"), GeneralUtil.getCurrentTimeStamp());
        // set where clause
        requestEntityCriteriaUpdate.where(cb.equal(root.get("requestId"),requestId));
        // update
        return persistenceManager.entityManager().createQuery(requestEntityCriteriaUpdate).executeUpdate();
    }

    @Override
    public int saveRequestGroupComments(String modifiedBy, String requestGroupId, String comments) {
        CriteriaBuilder cb = persistenceManager.entityManager().getCriteriaBuilder();
        CriteriaUpdate<RequestEvaluatorEntity> requestGroupEntityCriteriaUpdate = cb.createCriteriaUpdate(RequestEvaluatorEntity.class);
        Root<RequestEvaluatorEntity> root = requestGroupEntityCriteriaUpdate.from(RequestEvaluatorEntity.class);
        // update properties
        requestGroupEntityCriteriaUpdate.set(root.get("comments"), comments);
        requestGroupEntityCriteriaUpdate.set(root.get("modifiedUser"), modifiedBy);
        requestGroupEntityCriteriaUpdate.set(root.get("modifiedDate"), GeneralUtil.getCurrentTimeStamp());
        // set where clause
        requestGroupEntityCriteriaUpdate.where(cb.equal(root.get("requestEvaluatorId"),requestGroupId));
        // update
        return persistenceManager.entityManager().createQuery(requestGroupEntityCriteriaUpdate).executeUpdate();
    }

    /*
        DataBuild
     */

    @Override
    public List<RequestEntity> getDataBuildRequests(String userId) {
        return persistenceManager.query(RequestEntity.class, "from RequestEntity r where r.state like '" + Constants.requestStateConstants.get("committed") + "' and r.dataBuildRequired like 'Y')");
    }

    @Override
    public int updateDataBuildStatus(String requestId,String dataBuildState,String modifiedBy){
        CriteriaBuilder cb = persistenceManager.entityManager().getCriteriaBuilder();
        CriteriaUpdate<RequestEntity> requestEntityCriteriaUpdate = cb.createCriteriaUpdate(RequestEntity.class);
        Root<RequestEntity> root = requestEntityCriteriaUpdate.from(RequestEntity.class);

        char dataBuildCompletedStatus = dataBuildState.charAt(0);

        requestEntityCriteriaUpdate.set(root.get("dataBuildCompleted"), dataBuildCompletedStatus);
        requestEntityCriteriaUpdate.set(root.get("modifiedUser"), modifiedBy);
        requestEntityCriteriaUpdate.set(root.get("modifiedDate"), GeneralUtil.getCurrentTimeStamp());

        requestEntityCriteriaUpdate.where(cb.equal(root.get("requestId"),requestId));
        // update
        return persistenceManager.entityManager().createQuery(requestEntityCriteriaUpdate).executeUpdate();
    }

}

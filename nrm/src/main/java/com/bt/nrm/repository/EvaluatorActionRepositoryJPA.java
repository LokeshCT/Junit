package com.bt.nrm.repository;


import com.bt.nrm.dto.EvaluatorActionsDTO;
import com.bt.nrm.dto.RequestEvaluatorDTO;
import com.bt.nrm.dto.RequestEvaluatorPriceGroupDTO;
import com.bt.nrm.dto.UserGroupDTO;
import com.bt.nrm.repository.entity.RequestEvaluatorEntity;
import com.bt.nrm.repository.entity.RequestEvaluatorPriceGroupEntity;
import com.bt.nrm.util.Constants;
import com.bt.nrm.util.GeneralUtil;
import com.bt.rsqe.persistence.PersistenceManager;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaUpdate;
import javax.persistence.criteria.Root;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.bt.rsqe.utils.AssertObject.*;

public class EvaluatorActionRepositoryJPA implements EvaluatorActionRepository{

    private final PersistenceManager persistenceManager;

    public EvaluatorActionRepositoryJPA(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    @Override
    public Map<String, List<EvaluatorActionsDTO>> getAllEvaluatorActions(List<UserGroupDTO> userGroupList, String userId){

        // Criteria
        /*CriteriaBuilder qb = persistenceManager.entityManager().getCriteriaBuilder();
        CriteriaQuery<RequestGroupEntity>cq = qb.createQuery(RequestGroupEntity.class);
        Root<RequestEntity> root1 = cq.from(RequestEntity.class);
        Root<RequestGroupEntity> root2 = cq.from(RequestGroupEntity.class);
        Root<QuoteEntity> root3 = cq.from(QuoteEntity.class);

        Expression expression1 = qb.and(qb.equal(root2.get("serial"), root1.get("serial")), qb.equal(root2.get("type"), root1.get("type")) );
        Expression expression3 = qb.and(qb.equal(root2.get("serial"), root3.get("serial")), qb.equal(root2.get("type"), root3.get("type")) );

        cq.where(qb.and(expression1, expression3));
        Query testQuery = persistenceManager.entityManager().createQuery(cq);

        CriteriaBuilder criteriaBuilder = persistenceManager.entityManager().getCriteriaBuilder();

        CriteriaQuery<RequestGroupEntity> criteria = criteriaBuilder.createQuery(RequestGroupEntity.class);
        Root<RequestGroupEntity> companyRoot = criteria.from(RequestGroupEntity.class);
        Join<RequestGroupEntity,RequestEntity> products = companyRoot.join("requestEntity.requestId");
        Join<RequestEntity, QuoteEntity> cityJoin = companyRoot.join("address.city");//Company->Address->City-city
        criteria.where(criteriaBuilder.equal(products.get("category"), "dentist"),      criteriaBuilder.equal(cityJoin.get("city"),"Leeds"));

*/
        Map<String, List<EvaluatorActionsDTO>> evaluatorActionsMap = new HashMap<String, List<EvaluatorActionsDTO>>();

        if(isNotNull(userGroupList) && isNotNull(userId)) {
            Set<String> evaluatorGroupIds = new HashSet<String>();
            Set<String> productCategoryIds = new HashSet<String>();
            for(UserGroupDTO userGroupDTO : userGroupList){
                evaluatorGroupIds.add(userGroupDTO.getGroup().getEvaluatorGroupId());
                productCategoryIds.add(userGroupDTO.getProduct().getProductCategoryCode());
            }

            String queryPersonalActions = "SELECT RM.REQUEST_ID REQUEST_ID,\n" +
                    "  RM.REQUEST_NAME REQUEST_NAME,\n" +
                    "  COALESCE(QM.QUOTE_ID, QM.QUOTE_OPTION_ID) QUOTE_ID,\n" +
                    "  COALESCE(QM.QUOTE_NAME, QM.QUOTE_OPTION_NAME) QUOTE_NAME,\n" +
                    "  RM.PRODUCT_CATEGORY_NAME PRODUCT_CATEGORY_NAME,\n" +
                    "  RM.TEMPLATE_NAME TEMPLATE_NAME,\n" +
                    "  QM.CUSTOMER_NAME CUSTOMER_NAME,\n" +
                    "  RE.STATE STATE,\n" +
                    "  QM.SALES_CHANNEL_NAME SALES_CHANNEL_NAME,\n " +
                    "  RE.CREATED_DATE CREATED_DATE,\n" +
                    "  RE.ACCEPTED_DATE ACCEPTED_DATE,\n" +
                    "  RE.ACCEPTED_BY ACCEPTED_BY,\n" +
                    "  RE.EVALUATOR_GROUP_NAME EVALUATOR_GROUP_NAME,\n" +
                    "  RE.REQUEST_EVALUATOR_ID REQUEST_EVALUATOR_ID\n" +
                    "FROM REQUEST_EVALUATOR RE,\n" +
                    "  REQUEST_MASTER RM,\n" +
                    "  QUOTE_MASTER QM\n" +
                    "WHERE RE.EVALUATOR_GROUP_ID  IN (:evaluatorGroupIds)\n" +
                    "AND RE.ACCEPTED_BY            = (:acceptedByUserId)\n" +
                    "AND RE.REQUEST_ID             = RM.REQUEST_ID\n" +
                    "AND RM.PRODUCT_CATEGORY_CODE IN (:productCategoryIds)\n" +
                    "AND RM.STATE = '"+ Constants.requestStateConstants.get("signedIn") +"'\n" +
                    "AND RM.QUOTE_MASTER_ID        = QM.QUOTE_MASTER_ID";

            String queryAllActions = "SELECT RM.REQUEST_ID REQUEST_ID,\n" +
                    "  RM.REQUEST_NAME REQUEST_NAME,\n" +
                    "  COALESCE(QM.QUOTE_ID, QM.QUOTE_OPTION_ID) QUOTE_ID,\n" +
                    "  COALESCE(QM.QUOTE_NAME, QM.QUOTE_OPTION_NAME) QUOTE_NAME,\n" +
                    "  RM.PRODUCT_CATEGORY_NAME PRODUCT_CATEGORY_NAME,\n" +
                    "  RM.TEMPLATE_NAME TEMPLATE_NAME,\n" +
                    "  QM.CUSTOMER_NAME CUSTOMER_NAME,\n" +
                    "  RE.STATE STATE,\n" +
                    "  QM.SALES_CHANNEL_NAME SALES_CHANNEL_NAME,\n " +
                    "  RE.CREATED_DATE CREATED_DATE,\n" +
                    "  RE.ACCEPTED_DATE ACCEPTED_DATE,\n" +
                    "  RE.ACCEPTED_BY ACCEPTED_BY,\n" +
                    "  RE.EVALUATOR_GROUP_NAME EVALUATOR_GROUP_NAME,\n" +
                    "  RE.REQUEST_EVALUATOR_ID REQUEST_EVALUATOR_ID\n" +
                    "FROM REQUEST_EVALUATOR RE,\n" +
                    "  REQUEST_MASTER RM,\n" +
                    "  QUOTE_MASTER QM\n" +
                    "WHERE RE.EVALUATOR_GROUP_ID  IN (:evaluatorGroupIds)\n" +
                    "AND (RE.ACCEPTED_BY <> (:acceptedByUserId) OR RE.ACCEPTED_BY  is NULL)" +
                    "AND RE.REQUEST_ID             = RM.REQUEST_ID\n" +
                    "AND RM.PRODUCT_CATEGORY_CODE IN (:productCategoryIds)\n" +
                    "AND RM.STATE = '"+ Constants.requestStateConstants.get("signedIn") +"'\n" +
                    "AND RM.QUOTE_MASTER_ID        = QM.QUOTE_MASTER_ID";


            List<Object> listPersonalActions = persistenceManager.entityManager().createNativeQuery(queryPersonalActions).
                    setParameter("evaluatorGroupIds",evaluatorGroupIds).setParameter("acceptedByUserId",userId).setParameter("productCategoryIds",productCategoryIds).getResultList();
            List<EvaluatorActionsDTO> personalEvaluatorActions = new ArrayList<EvaluatorActionsDTO>();
            Iterator itrPerActions = listPersonalActions.iterator( );
            while (itrPerActions.hasNext( )) {
                Object[] result = (Object[])itrPerActions.next(); // Iterating through array object
                personalEvaluatorActions.add(new EvaluatorActionsDTO((String)result[0],//String
                        (String)result[1],//String
                        (String)result[2],//String
                        (String)result[3],//String
                        (String)result[4],//String
                        (String)result[5],//String
                        (String)result[6],//String
                        (String)result[7],//String
                        (String)result[8],//String
                        //(String)result[9],//Timestamp
                        result[9] != null ? new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(result[9]) : "",
                        //(String)result[10],//Date
                        result[10] != null ? new SimpleDateFormat("dd-MMM-yyyy").format(result[10]) : "",
                        (String)result[11],//String
                        (String)result[12],//String
                        (String)result[13]/*String*/));

            }
            //System.out.println("ListPersonalActions-->"+listPersonalActions);
            evaluatorActionsMap.put("PersonalActions",personalEvaluatorActions);

            List<Object> listAllActions = persistenceManager.entityManager().createNativeQuery(queryAllActions).
                    setParameter("evaluatorGroupIds",evaluatorGroupIds).setParameter("acceptedByUserId",userId).setParameter("productCategoryIds",productCategoryIds).getResultList();
            List<EvaluatorActionsDTO> allEvaluatorActions = new ArrayList<EvaluatorActionsDTO>();
            Iterator itrAllActions = listAllActions.iterator( );
            while (itrAllActions.hasNext( )) {
                Object[] result = (Object[])itrAllActions.next(); // Iterating through array object
                allEvaluatorActions.add(new EvaluatorActionsDTO((String)result[0],//String
                        (String)result[1],//String
                        (String)result[2],//String
                        (String)result[3],//String
                        (String)result[4],//String
                        (String)result[5],//String
                        (String)result[6],//String
                        (String)result[7],//String
                        (String)result[8],//String
                        //(String)result[9],//Timestamp
                        result[9] != null ? new SimpleDateFormat("dd-MMM-yyyy HH:mm:ss").format(result[9]) : "",
                        //(String)result[10],//Date
                        result[10] != null ? new SimpleDateFormat("dd-MMM-yyyy").format(result[10]) : "",
                        (String)result[11],//String
                        (String)result[12],//String
                        (String)result[13]/*String*/));
            }
            //System.out.println("ListAllActions-->"+listAllActions);
            evaluatorActionsMap.put("AllActions",allEvaluatorActions);

        }
        return evaluatorActionsMap;
    }

    @Override
    public int acceptAgentAction(RequestEvaluatorDTO requestEvaluatorDTO)
    {
            CriteriaBuilder cb = persistenceManager.entityManager().getCriteriaBuilder();
            CriteriaUpdate<RequestEvaluatorEntity> requestEvaluatorEntityCriteriaUpdate = cb.createCriteriaUpdate(RequestEvaluatorEntity.class);
            Root<RequestEvaluatorEntity> root = requestEvaluatorEntityCriteriaUpdate.from(RequestEvaluatorEntity.class);
            requestEvaluatorDTO.setAcceptedDate( new Date());
            requestEvaluatorDTO.setModifiedDate( new Date());
            // update properties
            requestEvaluatorEntityCriteriaUpdate.set(root.get("state"), Constants.requestEvaluatorStateConstants.get("requestEvaluatorState_accepted"));
            requestEvaluatorEntityCriteriaUpdate.set(root.get("acceptedBy"), requestEvaluatorDTO.getAcceptedBy());
            requestEvaluatorEntityCriteriaUpdate.set(root.get("acceptedByName"), requestEvaluatorDTO.getAcceptedByName());
            requestEvaluatorEntityCriteriaUpdate.set(root.get("acceptedDate"), requestEvaluatorDTO.getAcceptedDate());
            requestEvaluatorEntityCriteriaUpdate.set(root.get("modifiedUser"), requestEvaluatorDTO.getModifiedBy());
            requestEvaluatorEntityCriteriaUpdate.set(root.get("modifiedUserName"), requestEvaluatorDTO.getModifiedUserName());
            requestEvaluatorEntityCriteriaUpdate.set(root.get("modifiedDate"), requestEvaluatorDTO.getModifiedDate());
            // set where clause
            requestEvaluatorEntityCriteriaUpdate.where(cb.equal(root.get("requestEvaluatorId"),requestEvaluatorDTO.getRequestEvaluatorId()));
            // update
            return persistenceManager.entityManager().createQuery(requestEvaluatorEntityCriteriaUpdate).executeUpdate();

    }

    @Override
    public int updatePriceGroups(String requestId, Map<String,RequestEvaluatorPriceGroupDTO> requestEvaluatorPriceGroupDTOMap, String modifiedBy)
    {
        CriteriaBuilder cb = persistenceManager.entityManager().getCriteriaBuilder();
        int count = 0;
        for (Map.Entry<String,RequestEvaluatorPriceGroupDTO> entry : requestEvaluatorPriceGroupDTOMap.entrySet()) {
            CriteriaUpdate<RequestEvaluatorPriceGroupEntity> reqEvalPriceGrpEntityCriteriaUpdateEntity = cb.createCriteriaUpdate(RequestEvaluatorPriceGroupEntity.class);
            Root<RequestEvaluatorPriceGroupEntity> root = reqEvalPriceGrpEntityCriteriaUpdateEntity.from(RequestEvaluatorPriceGroupEntity.class);

            String key = entry.getKey();
            RequestEvaluatorPriceGroupDTO requestEvaluatorPriceGroupDTO = entry.getValue();
            // update properties
            reqEvalPriceGrpEntityCriteriaUpdateEntity.set(root.get("oneOffRecommendedRetail"), requestEvaluatorPriceGroupDTO.getOneOffRecommendedRetail());
            reqEvalPriceGrpEntityCriteriaUpdateEntity.set(root.get("recurringRecommendedRetail"), requestEvaluatorPriceGroupDTO.getRecurringRecommendedRetail());
            reqEvalPriceGrpEntityCriteriaUpdateEntity.set(root.get("nrcPriceToPartner"), requestEvaluatorPriceGroupDTO.getNrcPriceToPartner());
            reqEvalPriceGrpEntityCriteriaUpdateEntity.set(root.get("rcPriceToPartner"), requestEvaluatorPriceGroupDTO.getRcPriceToPartner());
            reqEvalPriceGrpEntityCriteriaUpdateEntity.set(root.get("oneOffCost"), requestEvaluatorPriceGroupDTO.getOneOffCost());
            reqEvalPriceGrpEntityCriteriaUpdateEntity.set(root.get("recurringCost"), requestEvaluatorPriceGroupDTO.getRecurringCost());
            reqEvalPriceGrpEntityCriteriaUpdateEntity.set(root.get("modifiedUser"), modifiedBy);
            reqEvalPriceGrpEntityCriteriaUpdateEntity.set(root.get("modifiedDate"), GeneralUtil.getCurrentTimeStamp());

            // set where clause
            System.out.println("Key is : "+key);
            System.out.println("RequestEvaluatorPriceGroupId is : "+requestEvaluatorPriceGroupDTO.getRequestEvaluatorPriceGroupId());
            reqEvalPriceGrpEntityCriteriaUpdateEntity.where(cb.equal(root.get("requestEvaluatorPriceGroupId"),requestEvaluatorPriceGroupDTO.getRequestEvaluatorPriceGroupId()));
            persistenceManager.entityManager().createQuery(reqEvalPriceGrpEntityCriteriaUpdateEntity).executeUpdate();
            count = count+1;
        }
        System.out.println("Updated row count : "+count);
        return count;
    }
}

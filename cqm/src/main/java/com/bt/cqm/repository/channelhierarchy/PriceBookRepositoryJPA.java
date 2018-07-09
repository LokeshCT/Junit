package com.bt.cqm.repository.channelhierarchy;

import com.bt.cqm.exception.PriceBookException;
import com.bt.rsqe.customerinventory.utils.Constants;
import com.bt.rsqe.persistence.PersistenceManager;

import javax.persistence.Query;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 11/03/14
 * Time: 17:17
 * To change this template use File | Settings | File Templates.
 */
public class PriceBookRepositoryJPA implements PriceBookRepository {

    private PersistenceManager persistenceManager;
    // private static final CustomerRepositoryJPALogger LOG = LogFactory.createDefaultLogger(CustomerRepositoryJPALogger.class);

    public PriceBookRepositoryJPA(PersistenceManager persistenceManager) {
        this.persistenceManager = persistenceManager;
    }

    public List<PriceBookEntity> getPriceBookDetails(long customerID) throws PriceBookException {
        List<PriceBookEntity> priceBookDetails = null;
        try{
             priceBookDetails = persistenceManager.query(PriceBookEntity.class, "select  c from PriceBookEntity c where " +
                " c.pbCustomerId = ?0 ", customerID);
        }
        catch(Exception e)
        {
            throw new PriceBookException("Price Book Details not found for Customer: " + customerID);
        }
        if (null!=priceBookDetails && priceBookDetails.size() == 0) {
            throw new PriceBookException("Price Book Details not found for Customer: " + customerID);
        }
        return priceBookDetails;
    }

    public String createPriceBook(String salesChannelId, String customerId, String customerName, String productName, String rrpVersion, String ptpVersion) {

        try {
            String submitter = Constants.USER_NAME;
            String shortDescription = "Price Book";
            int status = 0;
            Long dateTemp = (System.currentTimeMillis()/1000);

            Object[] priceBookDetails = (Object[]) persistenceManager.entityManager().createNativeQuery("SELECT  T.PRODKEY, T.PMF_BT_ID,U.PMF_CATEGORY_ID,U.PACKAGE_PRODUCT_NAME\n" +
                    "                     FROM ARADMIN.PPSR_LINK_PRODUCT_CHANNEL_V T,PPSR_OWNER.PPSR_PRICE_BOOK_MAST_EXP_V@REMEDY_PPSR_DBLINK.WORLD U\n" +
                    "                    WHERE BFG_ROLE_ID = '" + salesChannelId +
                    "'                      AND PRODUCT_FRIENDLY_NAME = '" + productName + "' AND T.PRODKEY=U.PACKAGE_PRODUCT_KEY AND ROWNUM < 2").getSingleResult();
            String requestSequenceId = (String) persistenceManager.entityManager().createNativeQuery("select to_char(max(c1) + 1, '000000000000000') from T5730").getSingleResult();

            Query query = persistenceManager.entityManager().createNativeQuery("INSERT INTO ARADMIN.T5730 (C2,C7,C8,C536870913,C536870914," +
                    "C536870915,C536870916,C536870917,C536870918,C536870921,C559101201,C560031251,C3,C5,C6,C1) " +
                    " VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            query.setParameter(1, submitter);
            query.setParameter(2, status);
            query.setParameter(3, shortDescription);
            query.setParameter(4, customerName);
            query.setParameter(5, (String) priceBookDetails[3]);
            query.setParameter(6, customerId);
            query.setParameter(7, (String) priceBookDetails[1]);
            query.setParameter(8, rrpVersion);
            query.setParameter(9, ptpVersion);
            query.setParameter(10, null);
            query.setParameter(11, (String) priceBookDetails[2]);
            query.setParameter(12, productName);
            query.setParameter(13, dateTemp.toString());
            query.setParameter(14, submitter);
            query.setParameter(15, dateTemp);
            query.setParameter(16, requestSequenceId);
            query.executeUpdate();
        } catch (Exception e) {
            return "-1";
        }
        return "0";
    }

}

package com.bt.cqm.repository.channelhierarchy;

import com.bt.cqm.exception.PriceBookException;

import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: 607982105
 * Date: 11/03/14
 * Time: 17:16
 * To change this template use File | Settings | File Templates.
 */
public interface PriceBookRepository {

    public List<PriceBookEntity> getPriceBookDetails(long customerID) throws PriceBookException;
    public String createPriceBook(String salesChannelId, String customerId, String customerName, String productName, String rrpVersion, String ptpVersion);
}

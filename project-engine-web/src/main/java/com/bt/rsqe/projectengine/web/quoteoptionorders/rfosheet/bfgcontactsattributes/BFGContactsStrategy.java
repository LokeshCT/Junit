package com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.bfgcontactsattributes;

import com.bt.rsqe.domain.project.InstanceCharacteristicNotFound;
import com.bt.rsqe.domain.project.ProductInstance;
import com.bt.rsqe.expedio.contact.BFGContactCreationFailureException;

import java.util.List;

import static com.bt.rsqe.projectengine.web.quoteoptionorders.rfosheet.RFOSheetModel.*;

public interface BFGContactsStrategy {
    public List<BFGContactAttribute> getBFGContactsAttributes(ProductInstance productInstance) throws InstanceCharacteristicNotFound;
    public void createAndPersistBFGContactID(RFORowModel rfoRowModel) throws BFGContactCreationFailureException, InstanceCharacteristicNotFound;
}

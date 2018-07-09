package com.bt.rsqe.customerinventory.service.rootAssetUpdater;

import com.bt.rsqe.bfgfacade.constants.BfgConstants;
import com.bt.rsqe.bfgfacade.domain.StoredProcedureParam;
import com.bt.rsqe.bfgfacade.queries.BfgCommands;
import com.bt.rsqe.bfgfacade.write.sp.AbstractGenericStoredProcedureParameter;
import com.bt.rsqe.customerinventory.dto.pricebook.PriceBookExtnDTO;

import java.math.BigDecimal;
import java.sql.Date;

public class CifCustomerDeltaServiceSPParameter extends AbstractGenericStoredProcedureParameter<RootAssetDto> {
    @StoredProcedureParam(name = "PIN_ROOT_ELEMENT_ID", byPassNullInput = false)
    public Long rootElementId;

    @StoredProcedureParam(name = "PIN_REL_ELEMENT_ID", byPassNullInput = false)
    public Long relElementId;

    @StoredProcedureParam(name = "PIV_REL_ELEMENT_TYPE", byPassNullInput = false)
    public String relElementType;

    @StoredProcedureParam(name = "PIN_ELEMENT_ID", byPassNullInput = false)
    public Long elementId;

    @StoredProcedureParam(name = "PIV_ELEMENT_TYPE", byPassNullInput = false)
    public String elementType;

    @StoredProcedureParam(name = "PIV_ELEMENT_ROLE", byPassNullInput = false)
    public String elementRole;

    @StoredProcedureParam(name = "PIV_ELEMENT_RESILIENCE", byPassNullInput = false)
    public String elementResilence;

    @StoredProcedureParam(name = "PIV_ELEMENT_RELATIONSHIP_TYPE", inOut = StoredProcedureParam.InOut.IN)
    public String elementRelationshipType;

    @StoredProcedureParam(name = "PIV_ELEMENT_RELATIONSHIP_NAME", inOut = StoredProcedureParam.InOut.IN)
    public String elementRelationshipName;

    @StoredProcedureParam(name = "PID_ELEMENT_CEASED_DATE", inOut = StoredProcedureParam.InOut.IN)
    public Date elementCeasedDate;

    @StoredProcedureParam(name = "PIV_ELEMENT_SOURCE_SYSTEM", inOut = StoredProcedureParam.InOut.IN)
    public String elementSourceSystem;

    @StoredProcedureParam(name = "PIV_ELEMENT_INS_PREF", inOut = StoredProcedureParam.InOut.IN)
    public String elementInsPref;

    @StoredProcedureParam(name = "PIV_ROOT_ELEMENT_TYPE", inOut = StoredProcedureParam.InOut.IN)
    public String rootElementType;

    @StoredProcedureParam(name = "PIN_ROOT_ELEMENT_IDENTIFIER", inOut = StoredProcedureParam.InOut.IN)
    public String rootElementIdentifier;

    @StoredProcedureParam(name = "VP_ERROR_CODE", inOut = StoredProcedureParam.InOut.OUT)
    public String errorCode;

    @StoredProcedureParam(name = "VP_ERROR_MESSAGE", inOut = StoredProcedureParam.InOut.OUT)
    public String errorMsg;

    public CifCustomerDeltaServiceSPParameter(RootAssetDto dto) {
        super(dto);
    }

    @Override
    protected void mapParameters(RootAssetDto dto) {
        this.rootElementId = dto.getRootElementId();
        this.relElementId = dto.getRelElementId();
        this.relElementType = dto.getRelElementType();
        final java.util.Date ceasedDate = dto.getElementCeasedDate();
        this.elementCeasedDate = null != ceasedDate?new Date(ceasedDate.getTime()):null;
        this.elementId = dto.getElementId();
        this.elementType = dto.getElementType();
        this.elementResilence = dto.getElementResilence();
        this.elementRelationshipType = dto.getElementRelationshipType();
        this.elementRelationshipName = dto.getElementRelationshipName();
        this.elementSourceSystem = BfgConstants.SOURCE_SYSTEM;
        this.elementInsPref = dto.getElementInsPref();
        this.rootElementType = dto.getRootElementType();
        this.rootElementIdentifier = dto.getRootElementIdentifier();
    }

    @Override
    public String getCommand() {
        return resolveNumberOfParams(BfgCommands.createCifDeltaService(), this);
    }

    @Override
    public String getErrorCode() {
        return errorCode;
    }

    @Override
    public String getErrorDescription() {
        return errorMsg;
    }
}

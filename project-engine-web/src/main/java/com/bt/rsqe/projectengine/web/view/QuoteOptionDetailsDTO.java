package com.bt.rsqe.projectengine.web.view;

import com.bt.rsqe.domain.QuoteOptionItemStatus;
import com.bt.rsqe.logging.Log;
import com.bt.rsqe.logging.LogFactory;
import com.bt.rsqe.logging.LogLevel;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

@XmlRootElement
@XmlAccessorType(XmlAccessType.NONE)
public class QuoteOptionDetailsDTO {
    private static final QuoteOptionDetailsDTOLogger LOG = LogFactory.createDefaultLogger(QuoteOptionDetailsDTOLogger.class);

    @XmlElement(name = "sEcho")
    public int pageNumber;
    @XmlElement(name = "iTotalDisplayRecords")
    public int totalDisplayRecords;
    @XmlElement(name = "iTotalRecords")
    public int totalRecords;
    @XmlElement
    private List<LineItem> lineItems;
    @XmlElement
    private boolean isDiscountRequested; //used in ajax data table
    @XmlElement
    private String userInfo;

    public QuoteOptionDetailsDTO(int pageNumber, int totalDisplayRecords, int totalRecords,
                                 List<LineItem> lineItems, boolean isDiscountRequested, String userInfo) {
        this.pageNumber = pageNumber;
        this.totalDisplayRecords = totalDisplayRecords;
        this.totalRecords = totalRecords;
        this.lineItems = lineItems;
        this.isDiscountRequested = isDiscountRequested;
        this.userInfo = userInfo;
    }

    public QuoteOptionDetailsDTO() {
        // for jaxb
    }

    public List<LineItem> getItems() {
        return lineItems;
    }

    public static class LineItem {
        @XmlElement
        private String id;
        @XmlElement
        private String name;
        @XmlElement
        private String productSCode;
        @XmlElement
        private String siteName;
        @XmlElement
        private String miniAddress;
        @XmlElement
        private String subLocationName;
        @XmlElement
        private String room;
        @XmlElement
        private String floor;
        @XmlElement
        private String action;
        @XmlElement
        private String offerName;
        @XmlElement
        private String contractTerm;
        @XmlElement
        private String status;
        @XmlElement
        private String discountStatus;
        @XmlElement
        private String configureUrl;
        @XmlElement
        private String offerDetailsUrl;
        @XmlElement
        private String productDetailsUrl;
        @XmlElement
        private boolean configurable; //used in ftl
        @XmlElement
        private String contract; //used in ftl
        @XmlElement
        private String errorMessage;
        @XmlElement
        private String validity;
        @XmlElement
        private String orderStatus;
        @XmlElement
        private String pricingStatus;
        @XmlElement
        private String ifcAction;
        @XmlElement
        private boolean forIfc;
        @XmlElement
        private String summary;
        @XmlElement
        private boolean isImportable;
        @XmlElement
        private boolean isInFrontCatalogue;
        @XmlElement
        private String serviceLevelAgreement;
        @XmlElement
        private boolean hasLineItemNotes;
        @XmlElement
        private String maintainerAgreement;
        @XmlElement
        private String remainingContractTerm;
        @XmlElement
        private boolean isProxyProduct;


        public LineItem(String id, String name, String miniAddress, String subLocationName, String room, String floor,String productSCode, String siteName, String contractTerm, String action, String offerName,
                        String status, String discountStatus, String configureUrl, String offerDetailsUrl,String productDetailsUrl, String errorMessage,
                        String validity, String orderStatus, String pricingStatus, String ifcAction, boolean forIfc, String contract,
                        String summary, boolean isImportable, boolean isInFrontCatalogue, String serviceLevelAgreement,boolean hasLineItemNotes,
                        String maintainerAgreement,String remainingContractTerm,boolean isProxyProduct) {
            this.id = id;
            this.name = name;
            this.productSCode = productSCode;
            this.siteName = siteName;
            this.miniAddress=miniAddress;
            this.subLocationName = subLocationName;
            this.room = room;
            this.floor = floor;
            this.contractTerm = contractTerm;
            this.action = action;
            this.offerName = offerName;
            this.status = status;
            this.discountStatus = discountStatus;
            this.configureUrl = configureUrl;
            this.offerDetailsUrl = offerDetailsUrl;
            this.productDetailsUrl = productDetailsUrl;
            this.errorMessage = errorMessage;
            this.validity = validity;
            this.orderStatus = orderStatus;
            this.ifcAction = ifcAction;
            this.forIfc = forIfc;
            this.contract = contract;
            this.configurable = !status.equals(QuoteOptionItemStatus.INITIALIZING.getDescription()) && !status.equals(QuoteOptionItemStatus.FAILED.getDescription());
            this.pricingStatus = pricingStatus;
            this.summary = summary;
            this.isImportable = isImportable;
            LOG.logConstructorCallGetHasLineItemNotesValue(hasLineItemNotes);
            this.hasLineItemNotes = hasLineItemNotes;
            this.isInFrontCatalogue=isInFrontCatalogue;
            this.serviceLevelAgreement=serviceLevelAgreement;
            this.maintainerAgreement=maintainerAgreement;
            this.remainingContractTerm = remainingContractTerm;
            this.isProxyProduct = isProxyProduct;
        }

        public LineItem() {
            //for jaxb
        }

        public String getId() {return id;}

        public String getName() {return name;}

        public String getProductSCode() {
            return productSCode;
        }

        public String getContractTerm() {
            return contractTerm;
        }

        public String getAction() {
            return action;
        }

        public String getOfferName() {
            return offerName;
        }

        public String getStatus() {
            return status;
        }

        public String getDiscountStatus() {
            return discountStatus;
        }

        public String getConfigureUrl() {
            return configureUrl;
        }

        public String getOfferDetailsUrl() {
            return offerDetailsUrl;
        }
        public String getProductDetailsUrl() { return productDetailsUrl; }
        public boolean isProxyAsset() { return isProxyProduct; }
        public String getSiteName() {
            return siteName;
        }

        public String getMiniAddress() {
            return miniAddress;
        }

        public String getSubLocationName(){ return subLocationName;}

        public String getRoom(){ return room;}

        public String getFloor(){ return floor;}

        public String getErrorMessage() {
            return errorMessage;
        }

        public boolean isConfigurable() {
            return configurable;
        }

        public String getValidity() {
            return validity;
        }

        public String getOrderStatus() {
            return orderStatus;
        }

        public String getPricingStatus() {
            return pricingStatus;
        }

        public String getIfcAction() {
            return ifcAction;
        }

        public String getSummary() {
            return summary;
        }

        public String getContract() {return contract;}

        public boolean getIsImportable() {
            return isImportable;
        }

        public boolean getHasLineItemNotes()
        {
            LOG.logGetHasLineItemNotesCalled(hasLineItemNotes);
            return hasLineItemNotes;
        }

        public String getRemainingContractTerm() {
            return remainingContractTerm;
        }
    }

    interface  QuoteOptionDetailsDTOLogger {
        @Log(level = LogLevel.INFO, loggerName = "QuoteOptionDetailsDTOLogger", format = "QuoteOptionDetailsDTO constructor called with [%s]")
        void logConstructorCallGetHasLineItemNotesValue(boolean hasLineItemNotes);
        @Log(level = LogLevel.INFO, loggerName = "QuoteOptionDetailsDTOLogger", format = "getHasLineItemNotes called returning [%s]")
        void logGetHasLineItemNotesCalled(boolean hasLineItemNotes);

    }
}

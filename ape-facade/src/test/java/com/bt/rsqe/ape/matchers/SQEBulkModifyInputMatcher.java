package com.bt.rsqe.ape.matchers;

import com.bt.rsqe.ape.SQEBulkModifyInput;
import com.bt.rsqe.matchers.CompositeMatcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import pricing.ape.bt.com.schemas.AsIs.AsIs;
import pricing.ape.bt.com.schemas.Configurations.Configurations;
import pricing.ape.bt.com.schemas.ExistingSiteDetails.ExistingSiteDetails;
import pricing.ape.bt.com.schemas.Leg.Leg;
import pricing.ape.bt.com.schemas.LegConfiguration.LegConfiguration;
import pricing.ape.bt.com.schemas.SalesUserDetails.SalesUserDetails;
import pricing.ape.bt.com.schemas.SiteDetails.SiteDetails;
import pricing.ape.bt.com.schemas.ToBe.ToBe;
import pricing.ape.bt.com.schemas.KGIDetails.KGIDetails;

import static com.bt.rsqe.utils.AssertObject.*;
import static java.lang.String.*;

public class SQEBulkModifyInputMatcher extends CompositeMatcher<SQEBulkModifyInput> {

    public static SQEBulkModifyInputMatcher aSQEBulkModifyInput() {
        return new SQEBulkModifyInputMatcher();
    }

    public SQEBulkModifyInputMatcher withSiteDetails(String siteName, String city, String countryName, String postCode, String streetNo,
                                                     String countryISOCode, String countyStateProvince,
                                                     String subLocality, String buildingNumber, String building, String subStreet, String subBuilding,
                                                     String subCountyStateProvince, String postalOrganisation, String pOBox, String stateCode,
                                                     String street, String locality, boolean decisionSupportFlag, KGIDetails kgiDetails, String fastConvergence, String multicast) {
        this.assertions.add(new SQEBulkModifyInputSiteDetailsMatcher(siteName, city, countryName, postCode, streetNo, countryISOCode,
                                                                     countyStateProvince, subLocality, buildingNumber, building, subStreet, subBuilding,
                                                                     subCountyStateProvince, postalOrganisation, pOBox, stateCode, street, locality,
                                                                     decisionSupportFlag, kgiDetails, fastConvergence, multicast));
        return this;
    }

    public SQEBulkModifyInputMatcher withUserDetails(String foreName, String surname, int ein, String emailId, String salesChannel) {
        this.assertions.add(new UserDetailsMatcher(foreName, surname, ein, emailId, salesChannel));
        return this;
    }

    public SQEBulkModifyInputMatcher withSynchUri(String synchUri) {
        this.assertions.add(new SynchUriMatcher(synchUri));
        return this;
    }

    public SQEBulkModifyInputMatcher withContractTerm(String contractTerm) {
        this.assertions.add(new ContractTermMatcher(contractTerm));
        return this;
    }

    public SQEBulkModifyInputMatcher withLegDetails(String type, String actionCode) {
        this.assertions.add(new LegDetailsMatcher(0, type, actionCode));
        return this;
    }

    public SQEBulkModifyInputMatcher withPrimaryLegDetails(String actionCode) {
        this.assertions.add(new LegDetailsMatcher(0, "Primary", actionCode));
        return this;
    }

    public SQEBulkModifyInputMatcher withSecondaryLegDetails(String actionCode) {
        this.assertions.add(new LegDetailsMatcher(1, "Secondary", actionCode));
        return this;
    }

    public SQEBulkModifyInputMatcher withConfiguration(boolean resilient, String productSla, String resiliencyType, String localCompanyName) {
        this.assertions.add(new ConfigurationMatcher(resilient, productSla, resiliencyType, localCompanyName));
        return this;
    }

    public SQEBulkModifyInputMatcher withToBeLegConfiguration(String portSpeed, String portSpeedUOM, String accessSpeed, String accessSpeedUom, String accessType, String accessSubType, String OldQref, boolean differentSiteMove) {
        this.assertions.add(new LegToBeConfigurationMatcher(0, portSpeed, portSpeedUOM, accessSpeed, accessSpeedUom, null, OldQref, accessType, accessSubType, differentSiteMove));
        return this;
    }

    public SQEBulkModifyInputMatcher withAsIsLegConfiguration(String portSpeed, String portSpeedUnit, String accessSpeed, String accessSpeedUnit, String accessTech, String accessType, String gPopNodeName, String supplier, String supplierProduct, String serviceVariant, boolean nonStandardFlag) {
        this.assertions.add(new LegAsIsConfigurationMatcher(0, portSpeed, portSpeedUnit, accessSpeed, accessSpeedUnit, accessTech, accessType, gPopNodeName, supplier, supplierProduct, serviceVariant, nonStandardFlag));
        return this;
    }

    public SQEBulkModifyInputMatcher withSecondaryToBeLegConfiguration(String portSpeed, String portSpeedUOM, String accessSpeed, String accessSpeedUom, String accessType, String accessSubType, String OldQref, boolean differentSiteMove) {
        this.assertions.add(new LegToBeConfigurationMatcher(1, portSpeed, portSpeedUOM, accessSpeed, accessSpeedUom, null, OldQref, accessType, accessSubType, differentSiteMove));
        return this;
    }

    public SQEBulkModifyInputMatcher withSecondaryAsIsLegConfiguration(String portSpeed, String portSpeedUnit, String accessSpeed, String accessSpeedUnit, String accessTech, String accessSubType, String gPopNodeName, String supplier, String supplierProduct, String serviceVariant, boolean nonStandardFlag) {
        this.assertions.add(new LegAsIsConfigurationMatcher(1, portSpeed, portSpeedUnit, accessSpeed, accessSpeedUnit, accessTech, accessSubType, gPopNodeName, supplier, supplierProduct, serviceVariant, nonStandardFlag));
        return this;
    }

    public SQEBulkModifyInputMatcher withNoSecondaryLeg() {
        this.assertions.add(new NoSecondaryLegMatcher());
        return this;
    }

    public SQEBulkModifyInputMatcher withNoPrimaryLegAsIsConfiguration() {
        this.assertions.add(new NoPrimaryLegAsIsMatcher());
        return this;
    }

    public SQEBulkModifyInputMatcher withNoPrimaryLegToBeConfiguration() {
        this.assertions.add(new NoPrimaryLegToBeMatcher());
        return this;
    }

    public SQEBulkModifyInputMatcher withNoSecondaryLegAsIsConfiguration() {
        this.assertions.add(new NoSecondaryLegAsIsMatcher());
        return this;
    }

    public SQEBulkModifyInputMatcher withNoSecondaryLegToBeConfiguration() {
        this.assertions.add(new NoSecondaryLegToBeMatcher());
        return this;
    }

    public SQEBulkModifyInputMatcher withExistingSiteDetails(String siteName, String city, String countryName, String postCode,
                                                             String countryISOCode, String countyStateProvince,
                                                             String subLocality, String buildingNumber, String building, String subStreet, String subBuilding,
                                                             String subCountyStateProvince, String postalOrganisation, String pOBox, String stateCode,
                                                             String street, String locality, KGIDetails kgiDetails){
        this.assertions.add(new SQEBulkModifyInputExistingSiteDetailsMatcher(siteName, city, countryName, postCode, countryISOCode,
                                                                             countyStateProvince, subLocality, buildingNumber, building, subStreet, subBuilding,
                                                                             subCountyStateProvince, postalOrganisation, pOBox, stateCode, street, locality,
                                                                             kgiDetails));
        return this;
    }

    private class SQEBulkModifyInputSiteDetailsMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {
        private final String siteName;
        private final String city;
        private final String countryName;
        private final String postCode;
        private final String streetNo;
        private final String countryISOCode;
        private final String countyStateProvince;
        private final String subLocality;
        private final String buildingNumber;
        private final String building;
        private final String subStreet;
        private final String subBuilding;
        private final String subCountyStateProvince;
        private final String postalOrganisation;
        private final String pOBox;
        private final String stateCode;
        private final String street;
        private final String locality;
        private boolean decisionSupportFlag;
        private String failure;
        private KGIDetails kgiDetails;
        private String fastConvergence;
        private String multicast;

        public SQEBulkModifyInputSiteDetailsMatcher(String siteName, String city, String countryName, String postCode, String streetNo, String countryISOCode, String countyStateProvince,
                                                    String subLocality, String buildingNumber, String building, String subStreet, String subBuilding,
                                                    String subCountyStateProvince, String postalOrganisation, String pOBox, String stateCode,
                                                    String street, String locality, boolean decisionSupportFlag, KGIDetails kgiDetails, String fastConvergence, String multicast) {
            this.siteName = siteName;
            this.city = city;
            this.countryName = countryName;
            this.postCode = postCode;
            this.streetNo = streetNo;
            this.countryISOCode = countryISOCode;
            this.countyStateProvince = countyStateProvince;
            this.subLocality = subLocality;
            this.buildingNumber = buildingNumber;
            this.building = building;
            this.subStreet = subStreet;
            this.subBuilding = subBuilding;
            this.subCountyStateProvince = subCountyStateProvince;
            this.postalOrganisation = postalOrganisation;
            this.pOBox = pOBox;
            this.stateCode = stateCode;
            this.street = street;
            this.locality = locality;
            this.decisionSupportFlag = decisionSupportFlag;
            this.kgiDetails=kgiDetails;
            this.fastConvergence = fastConvergence;
            this.multicast = multicast;
        }

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];

            if (!eq(siteName, siteDetails.getSiteName())) {
                failure = format("Site Name %s doesn't match with %s", siteDetails.getSiteName(), siteName);
            }
            if (!eq(city, siteDetails.getCity())) {
                failure = format("City %s doesn't match with %s", siteDetails.getCity(), city);
            }
            if (!eq(countryName, siteDetails.getCountryName())) {
                failure = format("Country %s doesn't match with %s", siteDetails.getCountryName(), countryName);
            }
            if (!eq(postCode, siteDetails.getPostCode())) {
                failure = format("Post code %s doesn't match with %s", siteDetails.getPostCode(), postCode);
            }
            if (!eq(streetNo, siteDetails.getStreetNo())) {
                failure = format("Telephone %s doesn't match with %s", siteDetails.getStreetNo(), streetNo);
            }
            if (!eq(countryISOCode, siteDetails.getCountryISOCode())) {
                failure = format("Country ISO Code %s doesn't match with %s", siteDetails.getCountryISOCode(), countryISOCode);
            }
            if (!eq(countyStateProvince, siteDetails.getCountyStateProvince())) {
                failure = format("County State Province %s doesn't match with %s", siteDetails.getCountyStateProvince(), countyStateProvince);
            }
            if (!eq(subLocality, siteDetails.getSubLocality())) {
                failure = format("Sub locality %s doesn't match with %s", siteDetails.getSubLocality(), subLocality);
            }
            if (!eq(buildingNumber, siteDetails.getBuildingNumber())) {
                failure = format("Building number %s doesn't match with %s", siteDetails.getBuildingNumber(), buildingNumber);
            }
            if (!eq(building, siteDetails.getBuilding())) {
                failure = format("Building %s doesn't match with %s", siteDetails.getBuilding(), building);
            }
            if (!eq(subStreet, siteDetails.getSubStreet())) {
                failure = format("Sub street %s doesn't match with %s", siteDetails.getSubStreet(), subStreet);
            }
            if (!eq(subCountyStateProvince, siteDetails.getSubCountyStateProvince())) {
                failure = format("Sub county state province %s doesn't match with %s", siteDetails.getSubCountyStateProvince(), subCountyStateProvince);
            }
            if (!eq(postalOrganisation, siteDetails.getPostalOrganisation())) {
                failure = format("Postal organisation %s doesn't match with %s", siteDetails.getPostalOrganisation(), postalOrganisation);
            }
            if (!eq(pOBox, siteDetails.getPOBox())) {
                failure = format("PO Box %s doesn't match with %s", siteDetails.getPOBox(), pOBox);
            }
            if (!eq(stateCode, siteDetails.getStateCode())) {
                failure = format("State code %s doesn't match with %s", siteDetails.getStateCode(), stateCode);
            }
            if (!eq(street, siteDetails.getStreet())) {
                failure = format("Street %s doesn't match with %s", siteDetails.getStreet(), street);
            }
            if (!eq(locality, siteDetails.getLocality())) {
                failure = format("Locality %s doesn't match with %s", siteDetails.getLocality(), locality);
            }
            if (!eq(subBuilding, siteDetails.getSubBuilding())) {
                failure = format("Sub building %s doesn't match with %s", siteDetails.getSubBuilding(), subBuilding);
            }

            if( decisionSupportFlag != siteDetails.isDSSEnabled())  {
                failure = format("Decision support flag %s doesn't match with expected %s", siteDetails.isDSSEnabled(), decisionSupportFlag);
            }

            if( !eq(fastConvergence, siteDetails.getFastconvergence()))  {
                failure = format("Fast Convergence %s doesn't match with expected %s", siteDetails.getFastconvergence(), fastConvergence);
            }

            KGIDetails siteKGI = siteDetails.getKgi();
            if( siteKGI.getAccuracyLevel() != kgiDetails.getAccuracyLevel())  {
                failure = format("Accuracy Level %s doesn't match with expected %s", kgiDetails.getAccuracyLevel(),siteKGI.getAccuracyLevel() );
            }

            return isNull(failure);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(failure);
        }
    }

    private class SQEBulkModifyInputExistingSiteDetailsMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {
        private final String siteName;
        private final String city;
        private final String countryName;
        private final String postCode;
        private final String countryISOCode;
        private final String countyStateProvince;
        private final String subLocality;
        private final String buildingNumber;
        private final String building;
        private final String subStreet;
        private final String subBuilding;
        private final String subCountyStateProvince;
        private final String postalOrganisation;
        private final String pOBox;
        private final String stateCode;
        private final String street;
        private final String locality;
        private String failure;
        private KGIDetails kgiDetails;

        public SQEBulkModifyInputExistingSiteDetailsMatcher(String siteName, String city, String countryName, String postCode, String countryISOCode, String countyStateProvince,
                                                    String subLocality, String buildingNumber, String building, String subStreet, String subBuilding,
                                                    String subCountyStateProvince, String postalOrganisation, String pOBox, String stateCode,
                                                    String street, String locality,KGIDetails kgiDetails) {
            this.siteName = siteName;
            this.city = city;
            this.countryName = countryName;
            this.postCode = postCode;
            this.countryISOCode = countryISOCode;
            this.countyStateProvince = countyStateProvince;
            this.subLocality = subLocality;
            this.buildingNumber = buildingNumber;
            this.building = building;
            this.subStreet = subStreet;
            this.subBuilding = subBuilding;
            this.subCountyStateProvince = subCountyStateProvince;
            this.postalOrganisation = postalOrganisation;
            this.pOBox = pOBox;
            this.stateCode = stateCode;
            this.street = street;
            this.locality = locality;
            this.kgiDetails=kgiDetails;
        }

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            ExistingSiteDetails siteDetails = sqeBulkModifyInput.getSites()[0].getExistingSiteDetails();

            if (!eq(siteName, siteDetails.getSiteName())) {
                failure = format("Site Name %s doesn't match with %s", siteDetails.getSiteName(), siteName);
            }
            if (!eq(city, siteDetails.getCity())) {
                failure = format("City %s doesn't match with %s", siteDetails.getCity(), city);
            }
            if (!eq(countryName, siteDetails.getCountryName())) {
                failure = format("Country %s doesn't match with %s", siteDetails.getCountryName(), countryName);
            }
            if (!eq(postCode, siteDetails.getPostCode())) {
                failure = format("Post code %s doesn't match with %s", siteDetails.getPostCode(), postCode);
            }
            if (!eq(countryISOCode, siteDetails.getCountryISOCode())) {
                failure = format("Country ISO Code %s doesn't match with %s", siteDetails.getCountryISOCode(), countryISOCode);
            }
            if (!eq(countyStateProvince, siteDetails.getCountyStateProvince())) {
                failure = format("County State Province %s doesn't match with %s", siteDetails.getCountyStateProvince(), countyStateProvince);
            }
            if (!eq(subLocality, siteDetails.getSubLocality())) {
                failure = format("Sub locality %s doesn't match with %s", siteDetails.getSubLocality(), subLocality);
            }
            if (!eq(buildingNumber, siteDetails.getBuildingNumber())) {
                failure = format("Building number %s doesn't match with %s", siteDetails.getBuildingNumber(), buildingNumber);
            }
            if (!eq(building, siteDetails.getBuilding())) {
                failure = format("Building %s doesn't match with %s", siteDetails.getBuilding(), building);
            }
            if (!eq(subStreet, siteDetails.getSubStreet())) {
                failure = format("Sub street %s doesn't match with %s", siteDetails.getSubStreet(), subStreet);
            }
            if (!eq(subCountyStateProvince, siteDetails.getSubCountyStateProvince())) {
                failure = format("Sub county state province %s doesn't match with %s", siteDetails.getSubCountyStateProvince(), subCountyStateProvince);
            }
            if (!eq(postalOrganisation, siteDetails.getPostalOrganisation())) {
                failure = format("Postal organisation %s doesn't match with %s", siteDetails.getPostalOrganisation(), postalOrganisation);
            }
            if (!eq(pOBox, siteDetails.getPOBox())) {
                failure = format("PO Box %s doesn't match with %s", siteDetails.getPOBox(), pOBox);
            }
            if (!eq(stateCode, siteDetails.getStateCode())) {
                failure = format("State code %s doesn't match with %s", siteDetails.getStateCode(), stateCode);
            }
            if (!eq(street, siteDetails.getStreet())) {
                failure = format("Street %s doesn't match with %s", siteDetails.getStreet(), street);
            }
            if (!eq(locality, siteDetails.getLocality())) {
                failure = format("Locality %s doesn't match with %s", siteDetails.getLocality(), locality);
            }
            if (!eq(subBuilding, siteDetails.getSubBuilding())) {
                failure = format("Sub building %s doesn't match with %s", siteDetails.getSubBuilding(), subBuilding);
            }

            KGIDetails siteKGI = siteDetails.getKgi();
            if( siteKGI.getAccuracyLevel() != kgiDetails.getAccuracyLevel())  {
                failure = format("Accuracy Level %s doesn't match with expected %s", kgiDetails.getAccuracyLevel(),siteKGI.getAccuracyLevel() );
            }

            return isNull(failure);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(failure);
        }
    }

    private class UserDetailsMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {
        private final String firstName;
        private final String lastname;
        private final int ein;
        private final String emailId;
        private final String salesChannel;
        private String failure;

        public UserDetailsMatcher(String firstName, String lastname, int ein, String emailId, String salesChannel) {
            this.firstName = firstName;
            this.lastname = lastname;
            this.ein = ein;
            this.emailId = emailId;
            this.salesChannel = salesChannel;
        }

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            SalesUserDetails salesUserDetails = sqeBulkModifyInput.getSalesUserDetails();
            if (!eq(salesUserDetails.getSalesUserFirstName(), firstName)) {
                failure = format("First name %s doesn't match with %s", salesUserDetails.getSalesUserFirstName(), firstName);
            }
            if (!eq(salesUserDetails.getSalesUserLastName(), lastname)) {
                failure = format("Last name %s doesn't match with %s", salesUserDetails.getSalesUserLastName(), lastname);
            }
            if (salesUserDetails.getSalesUserEin() != ein) {
                failure = format("EIN %s doesn't match with %s", salesUserDetails.getSalesUserEin(), ein);
            }
            if (!eq(salesUserDetails.getSalesUserEmailID(), emailId)) {
                failure = format("Email %s doesn't match with %s", salesUserDetails.getSalesUserEmailID(), emailId);
            }
            if (!eq(salesUserDetails.getSalesChannel(), salesChannel)) {
                failure = format("Sales channel %s doesn't match with %s", salesUserDetails.getSalesChannel(), salesChannel);
            }
            return isNull(failure);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(failure);
        }
    }

    private class SynchUriMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {
        private final String synchUri;

        public SynchUriMatcher(String synchUri) {
            this.synchUri = synchUri;
        }

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            return eq(sqeBulkModifyInput.getSyncURI(), synchUri);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(format("synch URI doesn't match with %s", synchUri));
        }
    }

    private class NoSecondaryLegMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
            Configurations configurations = siteDetails.getConfigurations();
            Leg[] arrayOfLeg = configurations.getLegDetails();
            return arrayOfLeg.length == 1 && arrayOfLeg[0].getType().getValue().equals("Primary");
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Secondary Leg constructed wrongly");
        }
    }

    private class NoPrimaryLegAsIsMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
            Configurations configurations = siteDetails.getConfigurations();
            Leg[] arrayOfLeg = configurations.getLegDetails();
            return arrayOfLeg[0].getType().getValue().equals("Primary") && arrayOfLeg[0].getLegConfiguration().getAsIs() == null;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Primary Leg AsIs configuration constructed wrongly");
        }
    }

    private class NoPrimaryLegToBeMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
            Configurations configurations = siteDetails.getConfigurations();
            Leg[] arrayOfLeg = configurations.getLegDetails();
            return arrayOfLeg[0].getType().getValue().equals("Primary") && arrayOfLeg[0].getLegConfiguration().getToBe() == null;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Primary Leg ToBe configuration constructed wrongly");
        }
    }

    private class NoSecondaryLegAsIsMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
            Configurations configurations = siteDetails.getConfigurations();
            Leg[] arrayOfLeg = configurations.getLegDetails();
            return arrayOfLeg[1].getType().getValue().equals("Secondary") && arrayOfLeg[1].getLegConfiguration().getAsIs() == null;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Secondary Leg AsIs configuration constructed wrongly");
        }
    }

    private class NoSecondaryLegToBeMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
            Configurations configurations = siteDetails.getConfigurations();
            Leg[] arrayOfLeg = configurations.getLegDetails();
            return arrayOfLeg[1].getType().getValue().equals("Secondary") && arrayOfLeg[1].getLegConfiguration().getToBe() == null;
        }

        @Override
        public void describeTo(Description description) {
            description.appendText("Secondary Leg ToBe configuration constructed wrongly");
        }
    }

    private class ContractTermMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {

        private String contractTerm;

        public ContractTermMatcher(String contractTerm) {
            this.contractTerm = contractTerm;
        }

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            return sqeBulkModifyInput.getTerm().equals(contractTerm);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(format("synch contract term doesn't match with %s", contractTerm));
        }
    }

    private class LegToBeConfigurationMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {
        private int legPointer;
        private final String supplier;
        private final String oldQref;
        private String portSpeed;
        private String uom;
        private String accessType;
        private String failure;
        private String accessSubType;
        private boolean differentSiteMove;
        private String accessSpeed;
        private String accessSpeedUom;

        public LegToBeConfigurationMatcher(int legPointer, String portSpeed, String portSpeedUOM, String accessSpeed, String accessSpeedUom, String supplier, String oldQref, String accessType, String accessSubType, boolean differentSiteMove) {
            this.legPointer = legPointer;
            this.supplier = supplier;
            this.oldQref = oldQref;
            this.portSpeed = portSpeed;
            this.uom = portSpeedUOM;
            this.accessType = accessType;
            this.accessSubType = accessSubType;
            this.differentSiteMove = differentSiteMove;
            this.accessSpeed = accessSpeed;
            this.accessSpeedUom = accessSpeedUom;
        }

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
            Configurations configurations = siteDetails.getConfigurations();
            Leg[] arrayOfLeg = configurations.getLegDetails();
            Leg leg = arrayOfLeg[legPointer];
            LegConfiguration legConfiguration = leg.getLegConfiguration();
            ToBe toBe = legConfiguration.getToBe();

            String currentLeg = legPointer == 0 ? "Primary Tobe" : "Secondary ToBe";

            if (!eq(toBe.getAccessSpeed(), accessSpeed)) {
                failure = format("%s Access Speed %s doesn't match with %s", currentLeg, toBe.getAccessSpeed(), accessSpeed);
            }
            if (!eq(toBe.getAccessSpeedUOM(), accessSpeedUom)) {
                failure = format("%s Access Speed UOM %s doesn't match with %s", currentLeg, toBe.getAccessSpeed(), accessSpeedUom);
            }
            if (!eq(toBe.getSupplier(), supplier)) {
                failure = format("%s Supplier %s doesn't match with %s", currentLeg, toBe.getSupplier(), supplier);
            }
            if (!eq(toBe.getOldQref(), oldQref)) {
                failure = format("%s Old qref %s doesn't match with %s", currentLeg, toBe.getOldQref(), oldQref);
            }
            if (!eq(toBe.getPortSpeed(), portSpeed)) {
                failure = format("%s Port Speed %s doesn't match with %s", currentLeg, toBe.getPortSpeed(), portSpeed);
            }
            if (!eq(toBe.getPortSpeedUOM(), uom)) {
                failure = format("%s Port Speed UOM %s doesn't match with %s", currentLeg, toBe.getPortSpeedUOM(), uom);
            }
            if (!eq(toBe.getAccesTypeName(), accessType)) {
                failure = format("%s Access Type %s doesn't match with %s", currentLeg, toBe.getAccesTypeName(), accessType);
            }
            if (!eq(toBe.getAccessTechnology(), accessSubType)) {
                failure = format("%s Access Sub Type %s doesn't match with %s", currentLeg, toBe.getAccessTechnology(), accessSubType);
            }
            if (leg.isDifferentSiteMove() != differentSiteMove) {
                failure = format("%s Different Site Move %s doesn't match with %s", currentLeg, leg.isDifferentSiteMove(), differentSiteMove);
            }
            return isNull(failure);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(failure);
        }
    }

    private class LegAsIsConfigurationMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {

        private final int legPointer;
        private final String portSpeed;
        private final String portSpeedUnit;
        private final String accessSpeed;
        private final String accessSpeedUnit;
        private final String accessTech;
        private final String accessType;
        private final String gPopNodeName;
        private final String supplier;
        private final String supplierProduct;
        private final String serviceVariant;
        private final boolean nonStandardFlag;
        private String failure;

        public LegAsIsConfigurationMatcher(int legPointer, String portSpeed, String portSpeedUnit, String accessSpeed, String accessSpeedUnit, String accessTech, String accessType, String gPopNodeName, String supplier, String supplierProduct, String serviceVariant, boolean nonStandardFlag) {

            this.legPointer = legPointer;
            this.portSpeed = portSpeed;
            this.portSpeedUnit = portSpeedUnit;
            this.accessSpeed = accessSpeed;
            this.accessSpeedUnit = accessSpeedUnit;
            this.accessTech = accessTech;
            this.accessType = accessType;
            this.gPopNodeName = gPopNodeName;
            this.supplier = supplier;
            this.supplierProduct = supplierProduct;
            this.serviceVariant = serviceVariant;
            this.nonStandardFlag = nonStandardFlag;
        }

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
            Configurations configurations = siteDetails.getConfigurations();
            Leg[] arrayOfLeg = configurations.getLegDetails();
            Leg leg = arrayOfLeg[legPointer];
            LegConfiguration legConfiguration = leg.getLegConfiguration();
            AsIs asIs = legConfiguration.getAsIs();

            String currentLeg = legPointer == 0 ? "Primary AsIs" : "Secondary AsIs";


            if (!eq(asIs.getPortSpeed(), portSpeed)) {
                failure = format("%s Port Speed %s doesn't match with %s", currentLeg, asIs.getPortSpeed(), portSpeed);
            }
            if (!eq(asIs.getPortSpeedUOM(), portSpeedUnit)) {
                failure = format("%s Port Speed UOM %s doesn't match with %s", currentLeg, asIs.getPortSpeedUOM(), portSpeedUnit);
            }
            if (!eq(asIs.getAccessSpeed(), accessSpeed)) {
                failure = format("%s Access Speed %s doesn't match with %s", currentLeg, asIs.getAccessSpeed(), accessSpeed);
            }
            if (!eq(asIs.getAccessSpeedUOM(), accessSpeedUnit)) {
                failure = format("%s Access Speed UOM %s doesn't match with %s", currentLeg, asIs.getAccessSpeedUOM(), accessSpeedUnit);
            }
            if (!eq(asIs.getAccesTypeName(), accessTech)) {
                failure = format("%s Access Technology %s doesn't match with %s", currentLeg, asIs.getAccesTypeName(), accessTech);
            }
            if (!eq(asIs.getAccessTechnology(), accessType)) {
                failure = format("%s Access Type %s doesn't match with %s", currentLeg, asIs.getAccessTechnology(), accessType);
            }
            if (!eq(asIs.getGpopNode(), gPopNodeName)) {
                failure = format("%s GPop Node %s doesn't match with %s", currentLeg, asIs.getGpopNode(), gPopNodeName);
            }
            if (!eq(asIs.getSupplierProduct(), supplierProduct)) {
                failure = format("%s Supplier Product %s doesn't match with %s", currentLeg, asIs.getSupplierProduct(), supplierProduct);
            }
            if (!eq(asIs.getServiceVariant(), serviceVariant)) {
                failure = format("%s Service Variant %s doesn't match with %s", currentLeg, asIs.getServiceVariant(), serviceVariant);
            }
            if (!eq(asIs.getSupplier(), supplier)) {
                failure = format("%s Supplier %s doesn't match with %s", currentLeg, asIs.getSupplier(), supplier);
            }
            if (asIs.isNonStandardPortFlag() != nonStandardFlag) {
                failure = format("%s Non Standard Flag %s doesn't match with %s", currentLeg, asIs.isNonStandardPortFlag(), nonStandardFlag);
            }

            return isNull(failure);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(failure);
        }
    }

    private class ConfigurationMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {
        private String failure;
        private boolean resilient;
        private String productSla;
        private String resiliencyType;
        private String localCompanyName;

        public ConfigurationMatcher(boolean resilient, String productSla, String resiliencyType, String localCompanyName) {
            this.resilient = resilient;
            this.productSla = productSla;
            this.resiliencyType = resiliencyType;
            this.localCompanyName = localCompanyName;
        }

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            SiteDetails siteDetails = sqeBulkModifyInput.getSites()[0];
            Configurations configurations = siteDetails.getConfigurations();

            if (configurations.isResilient() != resilient) {
                failure = format("Resilient %s doesn't match with %s", configurations.isResilient(), resilient);
            }
            if (!eq(configurations.getProductSLA(), productSla)) {
                failure = format("Product SLA %s doesn't match with %s", configurations.getProductSLA(), productSla);
            }
            if (!eq(configurations.getResiliencyType(), resiliencyType)) {
                failure = format("Resiliency Type %s doesn't match with %s", configurations.getResiliencyType(), resiliencyType);
            }
            if (!eq(configurations.getLocalCompanyName(), localCompanyName)) {
                failure = format("Local Company Name %s doesn't match with %s", configurations.getLocalCompanyName(), localCompanyName);
            }

            return isNull(failure);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(failure);
        }
    }


    private class LegDetailsMatcher extends TypeSafeMatcher<SQEBulkModifyInput> {
        private int legIdentifier;
        private String type;
        private String actionCode;
        private String failure;

        public LegDetailsMatcher(int legIdentifier, String type, String actionCode) {
            this.legIdentifier = legIdentifier;
            this.type = type;
            this.actionCode = actionCode;
        }

        @Override
        public boolean matchesSafely(SQEBulkModifyInput sqeBulkModifyInput) {
            Leg legDetails = sqeBulkModifyInput.getSites()[0].getConfigurations().getLegDetails()[legIdentifier];

            if (!eq(legDetails.getType().getValue(), type)) {
                failure = format("Type %s doesn't match with %s", legDetails.getType().getValue(), type);
            }
            if (!eq(legDetails.getActionCode().getValue(), actionCode)) {
                failure = format("Action Code %s doesn't match with %s", legDetails.getActionCode().getValue(), actionCode);
            }
            return isNull(failure);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(failure);
        }
    }

    boolean eq(String str1, String str2) {
        return !(str1 != null ? !str1.equals(str2) : str2 != null);
    }

}

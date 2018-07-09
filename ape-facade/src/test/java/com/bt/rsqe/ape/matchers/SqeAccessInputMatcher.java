package com.bt.rsqe.ape.matchers;

import com.bt.rsqe.ape.OnNetBuilding;
import com.bt.rsqe.ape.SqeAccessInput;
import com.bt.rsqe.ape.SqeQuoteInput;
import com.bt.rsqe.ape.SqeUserDetails;
import com.bt.rsqe.matchers.CompositeMatcher;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;

import static com.bt.rsqe.utils.AssertObject.*;
import static java.lang.String.*;

public class SqeAccessInputMatcher extends CompositeMatcher<SqeAccessInput> {

    public static SqeAccessInputMatcher aSqeAccessInput() {
        return new SqeAccessInputMatcher();
    }

    public SqeAccessInputMatcher withSiteDetails(String siteName, String city, String country, String postCode, String teplePhone,
                                                 Double longitude, Double latitude, String countryISOCode, String countyStateProvince,
                                                 String subLocality, String buildingNumber, String building, String subStreet, String subBuilding,
                                                 String subCountyStateProvince, String postalOrganisation, String pOBox, String stateCode,
                                                 String street, String locality, String onNetBuildingId, boolean decisionSupportFlag,Integer accuracyLevel){
        this.assertions.add(new SqeAccessInputSiteDetailsMatcher(siteName, city, country, postCode, teplePhone, longitude, latitude, countryISOCode,
                                                                 countyStateProvince, subLocality, buildingNumber, building, subStreet, subBuilding,
                                                                 subCountyStateProvince, postalOrganisation, pOBox, stateCode, street, locality, onNetBuildingId,
                                                                 decisionSupportFlag,accuracyLevel));
        return this;
    }

    public SqeAccessInputMatcher withPrimaryPortSpeed(String bandwidth) {
        this.assertions.add(new PrimaryPortBandwidthMatcher(bandwidth));
        return this;
    }

    public SqeAccessInputMatcher withPrimaryAccessTechnology(String accessTechnology) {
        this.assertions.add(new PrimaryAccessTechnologyMatcher(accessTechnology));
        return this;
    }

    public SqeAccessInputMatcher withSecondaryPortSpeed(String bandwidth) {
        this.assertions.add(new SecondaryPortBandwidthMatcher(bandwidth));
        return this;
    }

    public SqeAccessInputMatcher withPrimaryAccessSpeed(String bandwidth) {
        this.assertions.add(new PrimaryAccessSpeedBandwidthMatcher(bandwidth) );
        return this;
    }

    public SqeAccessInputMatcher withSecondaryAccessSpeed(String bandwidth) {
        this.assertions.add(new SecondaryAccessSpeedBandwidthMatcher(bandwidth) );
        return this;
    }

    public SqeAccessInputMatcher withAutoSelection(String autoSelection) {
        this.assertions.add(new AutoSelectionValueMatcher(autoSelection));
        return this;
    }

    public SqeAccessInputMatcher withUserDetails(String foreName, String surname, int ein, String emailId, String salesChannel) {
        this.assertions.add(new UserDetailsMatcher(foreName, surname, ein, emailId, salesChannel));
        return this;
    }

    public SqeAccessInputMatcher withSynchUri(String synchUri) {
        this.assertions.add(new SynchUriMatcher(synchUri));
        return this;
    }

    public SqeAccessInputMatcher withContractTerm(String contractTerm) {
        this.assertions.add(new ContractTermMatcher(contractTerm));
        return this;
    }

    public SqeAccessInputMatcher withPrimaryAccessTechSubType(String subType) {
        this.assertions.add(new PrimaryAccessSubTypeMatcher(subType));
        return this;
    }

    private class SqeAccessInputSiteDetailsMatcher extends TypeSafeMatcher<SqeAccessInput> {
        private final String siteName;
        private final String city;
        private final String country;
        private final String postCode;
        private final String telephone;
        private final Double longitude;
        private final Double latitude;
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
        private String onNetBuildingCode;
        private boolean decisionSupportFlag;
        private String failure;
        private final Integer accuracyLevel;

        public SqeAccessInputSiteDetailsMatcher(String siteName, String city, String country, String postCode, String telephone, Double longitude,
                                                Double latitude, String countryISOCode, String countyStateProvince,
                                                 String subLocality, String buildingNumber, String building, String subStreet, String subBuilding,
                                                 String subCountyStateProvince, String postalOrganisation, String pOBox, String stateCode,
                                                String street, String locality, String onNetBuildingCode, boolean decisionSupportFlag,
                                                Integer accuracyLevel) {
            this.siteName = siteName;
            this.city = city;
            this.country = country;
            this.postCode = postCode;
            this.telephone = telephone;
            this.longitude = longitude;
            this.latitude = latitude;
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
            this.onNetBuildingCode = onNetBuildingCode;
            this.decisionSupportFlag = decisionSupportFlag;
            this.accuracyLevel=accuracyLevel;
        }

        @Override
        public boolean matchesSafely(SqeAccessInput sqeAccessInput) {
            SqeQuoteInput sqeQuoteInput = sqeAccessInput.getQuoteInput()[0];

            if( !eq(siteName, sqeQuoteInput.getSiteName()) ) {
                failure = format("Site Name %s doesn't match with %s",sqeQuoteInput.getSiteName(), siteName);
            }
            if( !eq(city, sqeQuoteInput.getCity())) {
                failure = format("City %s doesn't match with %s",sqeQuoteInput.getCity(), city);
            }
            if( !eq(country, sqeQuoteInput.getCountryName())) {
                failure = format("Country %s doesn't match with %s",sqeQuoteInput.getCountryName(), country);
            }
            if( !eq(postCode, sqeQuoteInput.getPostCode())) {
                failure = format("Post code %s doesn't match with %s",sqeQuoteInput.getPostCode(), postCode);
            }
            if(!eq(sqeQuoteInput.getTelephoneNo(), telephone)) {
                failure = format("Telephone %s doesn't match with %s",sqeQuoteInput.getTelephoneNo(), telephone);
            }
            if( sqeQuoteInput.getLongitude() !=longitude ) {
                failure = format("Longitude %s doesn't match with %s",sqeQuoteInput.getLongitude(), longitude);
            }
            if( sqeQuoteInput.getLatitude() != latitude) {
                failure = format("Latitude %s doesn't match with %s",sqeQuoteInput.getLatitude(), latitude);
            }
            if( !eq(countryISOCode, sqeQuoteInput.getCountryISOCode()) ) {
                failure = format("Country ISO Code %s doesn't match with %s",sqeQuoteInput.getCountryISOCode(), countryISOCode);
            }
            if( !eq(countyStateProvince, sqeQuoteInput.getCountyStateProvince()) ) {
                failure = format("County State Province %s doesn't match with %s",sqeQuoteInput.getCountyStateProvince(), countyStateProvince);
            }
            if( !eq(subLocality, sqeQuoteInput.getSubLocality()) ) {
                failure = format("Sub locality %s doesn't match with %s",sqeQuoteInput.getSubLocality(), subLocality);
            }
            if( !eq(buildingNumber, sqeQuoteInput.getBuildingNumber()) ) {
                failure = format("Building number %s doesn't match with %s",sqeQuoteInput.getBuildingNumber(), buildingNumber);
            }
            if( !eq(building, sqeQuoteInput.getBuilding()) ) {
                failure = format("Building %s doesn't match with %s",sqeQuoteInput.getBuilding(), building);
            }
            if( !eq(subStreet, sqeQuoteInput.getSubStreet()) ) {
                failure = format("Sub street %s doesn't match with %s",sqeQuoteInput.getSubStreet(), subStreet);
            }
            if( !eq(subCountyStateProvince, sqeQuoteInput.getSubCountyStateProvince()) ) {
                failure = format("Sub county state province %s doesn't match with %s",sqeQuoteInput.getSubCountyStateProvince(), subCountyStateProvince);
            }
            if( !eq(postalOrganisation, sqeQuoteInput.getPostalOrganisation()) ) {
                failure = format("Postal organisation %s doesn't match with %s",sqeQuoteInput.getPostalOrganisation(), postalOrganisation);
            }
            if( !eq(pOBox, sqeQuoteInput.getPOBox()) ) {
                failure = format("PO Box %s doesn't match with %s",sqeQuoteInput.getPOBox(), pOBox);
            }
            if( !eq(stateCode, sqeQuoteInput.getStateCode()) ) {
                failure = format("State code %s doesn't match with %s",sqeQuoteInput.getStateCode(), stateCode);
            }
            if( !eq(street, sqeQuoteInput.getStreet()) ) {
                failure = format("Street %s doesn't match with %s",sqeQuoteInput.getStreet(), street);
            }
            if( !eq(locality, sqeQuoteInput.getLocality()) ) {
                failure = format("Locality %s doesn't match with %s",sqeQuoteInput.getLocality(), locality);
            }
            if( !eq(subBuilding, sqeQuoteInput.getSubBuilding()) ) {
                failure = format("Sub building %s doesn't match with %s",sqeQuoteInput.getSubBuilding(), subBuilding);
            }

            OnNetBuilding[] listOfOnNetBuildingCodes = sqeQuoteInput.getListOfOnNetBuildingCodes();
            if( !eq(onNetBuildingCode, listOfOnNetBuildingCodes[0].getOnNetBuildingCode()) ) {
                failure = format("On-net building code %s doesn't match with %s",listOfOnNetBuildingCodes[0].getOnNetBuildingCode(), onNetBuildingCode);
            }

            if( decisionSupportFlag != sqeQuoteInput.isDSSEnabled())  {
                failure = format("Decision support flag %s doesn't match with expected %s", sqeQuoteInput.isDSSEnabled(), decisionSupportFlag);
            }

            if( sqeQuoteInput.getAccuracylevel() !=accuracyLevel ) {
                failure = format("Accuracy Level %s doesn't match with %s",sqeQuoteInput.getAccuracylevel(), accuracyLevel);
            }

            return isNull(failure);
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(failure);
        }
    }

    private abstract class AbstractAccessInputMatcher extends TypeSafeMatcher<SqeAccessInput> {

        private final String expectedValue;

        public AbstractAccessInputMatcher(String expectedValue) {
            this.expectedValue = expectedValue;
        }

        @Override
        public boolean matchesSafely(SqeAccessInput sqeAccessInput) {
            return expectedValue.equals( getActualValue(sqeAccessInput) );
        }

        @Override
        public void describeTo(Description description) {
            description.appendText(format("%s doesn't match with %s", getParameterName(), expectedValue));
        }

        protected abstract String getActualValue(SqeAccessInput sqeAccessInput);
        protected abstract String getParameterName();
    }

    private class PrimaryPortBandwidthMatcher extends AbstractAccessInputMatcher{
        public PrimaryPortBandwidthMatcher(String expectedValue) {
            super(expectedValue);
        }

        @Override
        protected String getActualValue(SqeAccessInput sqeAccessInput) {
            return String.format("%s%s", sqeAccessInput.getQuoteInput()[0].getPortSpeed1(), sqeAccessInput.getQuoteInput()[0].getPortSpeedUom1());
        }

        @Override
        protected String getParameterName() {
            return "Primary Port Speed";
        }
    }

    private class SecondaryPortBandwidthMatcher extends AbstractAccessInputMatcher{
        public SecondaryPortBandwidthMatcher(String expectedValue) {
            super(expectedValue);
        }

        @Override
        protected String getActualValue(SqeAccessInput sqeAccessInput) {
            return String.format("%s%s", sqeAccessInput.getQuoteInput()[0].getPortSpeed2(), sqeAccessInput.getQuoteInput()[0].getPortSpeedUom2());
        }

        @Override
        protected String getParameterName() {
            return "Secondary Port Speed";
        }
    }

    private class PrimaryAccessSpeedBandwidthMatcher extends AbstractAccessInputMatcher {

        public PrimaryAccessSpeedBandwidthMatcher(String bandwidth) {
            super(bandwidth);
        }

        @Override
        protected String getActualValue(SqeAccessInput sqeAccessInput) {
            return format("%s%s", sqeAccessInput.getQuoteInput()[0].getAccessSpeed1(), sqeAccessInput.getQuoteInput()[0].getAccessSpeedUom1());
        }

        @Override
        protected String getParameterName() {
            return "Primary Access Speed";
        }
    }

    private class SecondaryAccessSpeedBandwidthMatcher extends AbstractAccessInputMatcher {

        public SecondaryAccessSpeedBandwidthMatcher(String bandwidth) {
            super(bandwidth);
        }

        @Override
        protected String getActualValue(SqeAccessInput sqeAccessInput) {
            return format("%s%s", sqeAccessInput.getQuoteInput()[0].getAccessSpeed2(), sqeAccessInput.getQuoteInput()[0].getAccessSpeedUom2());
        }

        @Override
        protected String getParameterName() {
            return "Secondary Access Speed";
        }
    }


    private class PrimaryAccessTechnologyMatcher extends AbstractAccessInputMatcher {

        public PrimaryAccessTechnologyMatcher(String accessTechnology) {
            super(accessTechnology);
        }

        @Override
        protected String getActualValue(SqeAccessInput sqeAccessInput) {
            return format("%s", sqeAccessInput.getQuoteInput()[0].getAccessTechnology());
        }

        @Override
        protected String getParameterName() {
            return "Primary Access Technology";
        }
    }

    private class AutoSelectionValueMatcher extends AbstractAccessInputMatcher {

        public AutoSelectionValueMatcher(String autoSelectionValue) {
            super(autoSelectionValue);
        }

        @Override
        protected String getActualValue(SqeAccessInput sqeAccessInput) {
            return sqeAccessInput.getAutoSelection();
        }

        @Override
        protected String getParameterName() {
            return "AutoSelection";
        }
    }

    private class PrimaryAccessSubTypeMatcher extends AbstractAccessInputMatcher{
        public PrimaryAccessSubTypeMatcher(String expectedValue) {
            super(expectedValue);
        }

        @Override
        protected String getActualValue(SqeAccessInput sqeAccessInput) {
            return String.format("%s", sqeAccessInput.getQuoteInput()[0].getAccessTechnology());
        }

        @Override
        protected String getParameterName() {
            return "Primary Access Technology Sub Type";
        }
    }

    private class UserDetailsMatcher extends TypeSafeMatcher<SqeAccessInput> {
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
        public boolean matchesSafely(SqeAccessInput sqeAccessInput) {
            SqeUserDetails sqeUserDetails = sqeAccessInput.getSqeUserDetails();
            if( !eq(sqeUserDetails.getSalesUserFirstName(), firstName)) {
                failure = format("First name %s doesn't match with %s",sqeUserDetails.getSalesUserFirstName(), firstName);
            }
            if( !eq(sqeUserDetails.getSalesUserLastName(), lastname)) {
                failure = format("Last name %s doesn't match with %s",sqeUserDetails.getSalesUserLastName(), lastname);
            }
            if( sqeUserDetails.getSalesUserEin() !=  ein) {
                failure = format("EIN %s doesn't match with %s",sqeUserDetails.getSalesUserEin(), ein);
            }
            if( !eq(sqeUserDetails.getSalesUserEmailID(), emailId)) {
                failure = format("Email %s doesn't match with %s",sqeUserDetails.getSalesUserEmailID(), emailId);
            }
            if( !eq(sqeUserDetails.getSalesChannel(), salesChannel)) {
                failure = format("Sales channel %s doesn't match with %s",sqeUserDetails.getSalesChannel(), salesChannel);
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

    private class SynchUriMatcher extends AbstractAccessInputMatcher {

        public SynchUriMatcher(String synchUri) {
            super(synchUri);
        }

        @Override
        protected String getActualValue(SqeAccessInput sqeAccessInput) {
            return sqeAccessInput.getSyncURI();
        }

        @Override
        protected String getParameterName() {
            return "SynchUri";
        }
    }

    private class ContractTermMatcher extends AbstractAccessInputMatcher {


        public ContractTermMatcher(String contractTerm) {
            super(contractTerm);
        }

        @Override
        protected String getActualValue(SqeAccessInput sqeAccessInput) {
            return sqeAccessInput.getTerm();
        }

        @Override
        protected String getParameterName() {
            return "Contract Term";
        }
    }
}

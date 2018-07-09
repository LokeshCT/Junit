#!/usr/bin/bash


# get the existing customerId, projectId, quoteOptionId
# Original user:
CUSTOMER_ID=170556
PROJECT_ID=000000000192637
GUID=IDG2238646272ANMKWWG2GXES6CJ990000000001926371428649350#
CONTRACT_ID=441954
QUOTE_OPTION_NAME=blah
# Performance user
#CUSTOMER_ID=182913
#PROJECT_ID=000000000199758
#GUID=07fb87d7a1d74d7e91ccd9c0b45eec59000000000199758
#CONTRACT_ID=444128
#QUOTE_OPTION_NAME=blahblah

# TODO:  Add the CSS/JQuery extractor after the first request to get the contract!!
# TODO:  Add the CSS/JQuery extractor after the last request in the 002-CreatQuoteOption to get the QUOTE_OPTION_NAME!!

# generic capture of the QUOTE_OPTION_ID_LIST
#/quote-options\/42a7803f-5bae-4d91-82c3-de3ac7c93769/ {
#QUOTE_OPTION_ID=42a7803f-5bae-4d91-82c3-de3ac7c93769
QUOTE_OPTION_ID_LIST=($(cat icg-flow-test-plan.jmx | sed -n "s/^.*quote-options\/\([^-]\{8\}[-][^-]\{4\}[-][^-]\{4\}[-][^-]\{4\}[-][^-]\{12\}\).*$/\1/p" | uniq))

echo "quote-option-id = [$QUOTE_OPTION_ID_LIST]"


# ignore lines with testname="; replace the 
sed -i "{
   # ignore any testname definitions
   /testname=\"/! {

      # ignore the global property multi-line section
      /<elementProp\ name=\"JMETER_ROOT_DIR/,/<\/collectionProp/! {

	 #s/000000000192637/BOBBBBBBB/g

         # replace the quote option name
         s/$QUOTE_OPTION_NAME/\${QUOTE_OPTION_NAME}/g
	 
         # replace the quote option name
         s/$QUOTE_OPTION_ID_LIST/\${QUOTE_OPTION_ID}/g

         # replace the guid
         s/$GUID/\${RSQE_URL_GUID}/g
	 
         # replace the customerId
         s/$CUSTOMER_ID/\${RSQE_CUSTOMER_ID}/g
	 s/1079499/\${RSQE_CUSTOMER_ID}/g

         # replace the projectId
         s/$PROJECT_ID/\${RSQE_PROJECT_ID}/g

         # replace the contract id
         s/$CONTRACT_ID/\${RSQE_CONTRACT_ID}/g


         # replace the full quote option uri with ${QUOTE_OPTION_URI}
      }
   }
}" $1





# Hostfully Connector
Spring Batch processor for loading client data from the Hostfully PMS API.

## Requirements
* Java 8

## IntelliJ IDEA Debug Configuration
* Program Arguments: `--organizationId=XXX` use the `entityId` of the organization
* JRE: >= Java 8
* Active Profiles: `local`
* Override parameters: `user.timezone = UTC`

## Compiling with Maven
```shell script
$ ./mvnw compile
```

## Local Debug Setup
1. Run the `SafelyAPI` application which should be serving requests at http://localhost:8080
2. Run `HostfullyConnector` with the `organizationId` parameter set to an Organization `entityId` that is setup for testing.

## Organization Configuration Values
```json
{
    "_id" : "<alphanumeric-id",
    "entityId": "<alphanumeric-d>",
    "legacyOrganizationId" : <integer-id>,
    "version" : NumberLong(1),
    "name" : "Acme Property Management",
    "status" : "ACTIVE",
    "type" : "PROPERTY_MANAGER",
    "reservationSource" : "HOSTFULLY",
    "insuranceDefault" : true,
    "verificationDefault" : true,
    "startDate" : ISODate("2020-03-01T00:00:00.000Z"),
    "legacyPropertyMode" : "EXCLUDE_DEACTIVATED",
    "connectorOperationMode" : "ALL",
    "configurations": [
        {
          "effectiveStartDate" : ISODate("2020-01-01T00:00:00.000Z"),
          "effectiveEndDate" : null,
          "addNewProperties" : true,
          "insuranceDefault" : true,
          "verificationDefault" : true,
          "insureReservationWhenDropsBelowLongTermExclusion" : false,
          "legacyPropertyAccessMode" : "FULL_ACCESS",
          "legacyReservationAccessMode" : "READ_ONLY",
          "startDate" : ISODate("2020-01-01T00:00:00.000Z"),
          "endDate" : null,
          "legacyBookingStartDate" : null,
          "legacyBookingEndDate" : null,
          "legacyArrivalStartDate" : null,
          "legacyArrivalEndDate" : null,
          "safelyLegacyPortalApiCredentials" : {
              "name" : "Safely Legacy API",
              "source" : "SAFELY_API",
              "account_key" : ""
          },
          "pmsCredentials" : {
              "name" : "Hostfully",
              "source" : "HOSTFULLY",
              "custom_credentials_data" : {
                  "<key-based-on-api-needs>" : "<token-or-other-client-specific-value>"
              },
              "account_key" : "",
              "account_private_key" : ""
          },
          "organizationReservationCategory" : {
              "sequenceNumber" : 0,
              "category1" : [ 
                  "owner", 
                  "owner_guest"
              ],
              "category2" : [ 
                  "canceled", 
                  "declined"
              ],
              "insured" : false,
              "verifications" : false
          }
        }   
    ],
    "organizationSourceCredentials" : {
        "name" : "Hostfully",
        "source" : "HOSTFULLY",
        "custom_credentials_data" : {
            "<key-based-on-api-needs>" : "<token-or-other-client-specific-value>"
        }
    },
    "organizationReservationCategories" : [ 
        {
            "sequenceNumber" : 0,
            "category1" : [ 
                "GC"
            ],
            "insured" : false,
            "verifications" : false
        }
    ],
    "pmsPropertyTypesToSafelyTypesMapping" : {
        "HOUSE" : "SINGLE_FAMILY_HOME",
        "HOTEL" : "OTHER",
        "LODGE" : "OTHER",
        "B&B" : "OTHER",
        "RV" : "OTHER",
        "CONDO" : "OTHER"
    },
    "pmsReservationTypesToSafelyReservationTypesMapping" : {
        "G" : "STANDARD_GUEST",
        "OS" : "OWNER",
        "LT" : "LONG_TERM_GUEST",
        "OTA1" : "STANDARD_GUEST",
        "OTA2" : "STANDARD_GUEST",
        "OTA" : "STANDARD_GUEST"
    },
    "pmsChannelTypesToSafelyBookingChannelTypesMapping" : {
        "OTA1" : "HOMEAWAY",
        "OTA2" : "AIRBNB",
        "OTA" : "BOOKING_COM",
        "G" : "OTHER"
    },
    "legacyBookingStartDate" : ISODate("2020-02-01T00:00:00.000Z"),
    "_class" : "com.safely.api.domain.Organization"
}
 ``` 

When pulling data from the PMS, the connector will use the lesser of the `startDate`, `legacyBookingStartDate`, and `legacyArrivalStartDate` to determine the earliest date to load reservations.

The `pmsPropertyTypesToSafelyTypesMapping`, `pmsReservationTypesToSafelyReservationTypesMapping` and `pmsChannelTypesToSafelyBookingChanelTypesMapping` objects are not required to successfully load data from PMS. However, they should be populated when possible to better inform the mapping of data to a standardized form so that we can improve our analytics.

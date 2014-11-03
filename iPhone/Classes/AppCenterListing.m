//
//  AppCenterListing.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 03/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "AppCenterListing.h"
#import "JsonParser.h"

@implementation AppCenterListing

-(id)initWithJSON:(NSDictionary *)json
{
    self = [super init];
    if (self) {
        
        self.listingID = [JsonParser getNodeAsString:@"listingID" json:json];
        self.name = [JsonParser getNodeAsString:@"name" json:json];
        self.partnerName = [JsonParser getNodeAsString:@"partnerName" json:json];
        self.shortDescription = [JsonParser getNodeAsString:@"shortDescription" json:json];
        self.companyID = [JsonParser getNodeAsString:@"companyID" json:json];
        self.connectURL = [JsonParser getNodeAsString:@"connectURL" json:json];
        self.contactPhone = [JsonParser getNodeAsString:@"contactPhone" json:json];
        self.partnerListingURL = [JsonParser getNodeAsString:@"partnerListingURL" json:json];
        self.partnerImageURL = [JsonParser getNodeAsString:@"partnerImageURL" json:json];
        self.partnerAppID = [JsonParser getNodeAsString:@"partnerAppID" json:json];
        self.partnerAppConsumerKey = [JsonParser getNodeAsString:@"partnerAppConsumerKey" json:json];
        self.isUserConnected = [JsonParser getNodeAsBOOL:@"isUserConnected" json:json];
        self.launchAppIfAvailable = [JsonParser getNodeAsBOOL:@"launchAppIfAvailable" json:json];
        self.imageURL = [JsonParser getNodeAsString:@"imageURL" json:json];
        self.iosIconURL = [JsonParser getNodeAsString:@"iosIconURL" json:json];
        self.listingName = [JsonParser getNodeAsString:@"listingName" json:json];
        self.androidPackageName = [JsonParser getNodeAsString:@"androidPackageName" json:json];
        self.appStoreURL = [JsonParser getNodeAsString:@"appStoreURL" json:json];
        self.mobileSiteURL = [JsonParser getNodeAsString:@"mobileSiteURL" json:json];
        self.iosLaunchURL = [JsonParser getNodeAsString:@"iosLaunchURL" json:json];
        self.params = [JsonParser getNodeAsString:@"params" json:json];
    }
    return self;
}

@end
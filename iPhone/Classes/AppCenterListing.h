//
//  AppCenterListing.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 03/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AppCenterListing : NSObject

@property (nonatomic, strong)   NSString *listingID;
@property (nonatomic, strong)   NSString *name;
@property (nonatomic, strong)   NSString *partnerName;
@property (nonatomic, strong)   NSString *shortDescription;
@property (nonatomic, strong)   NSString *companyID;
@property (nonatomic, strong)   NSString *connectURL;
@property (nonatomic, strong)   NSString *contactPhone;
@property (nonatomic, strong)   NSString *partnerListingURL;
@property (nonatomic, strong)   NSString *partnerImageURL;
@property (nonatomic, strong)   NSString *partnerAppID;
@property (nonatomic, strong)   NSString *partnerAppConsumerKey;
@property (nonatomic)           BOOL isUserConnected;
@property (nonatomic)           BOOL launchAppIfAvailable;
@property (nonatomic, strong)   NSString *imageURL;
@property (nonatomic, strong)   NSString *iosIconURL;
@property (nonatomic, strong)   NSString *listingName;
@property (nonatomic, strong)   NSString *androidPackageName;
@property (nonatomic, strong)   NSString *appStoreURL;
@property (nonatomic, strong)   NSString *mobileSiteURL;
@property (nonatomic, strong)   NSString *iosLaunchURL;
@property (nonatomic, strong)   NSString *params;

-(id)initWithJSON:(NSDictionary *)json;

@end
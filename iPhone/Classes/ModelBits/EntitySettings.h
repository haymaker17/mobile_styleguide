//
//  EntitySettings.h
//  ConcurMobile
//
//  Created by Shifan Wu on 10/3/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntitySettings : NSManagedObject

@property (nonatomic, retain) NSNumber * isTripItEmailAddressConfirmed;
@property (nonatomic, retain) NSString * saveUserName;
@property (nonatomic, retain) NSString * disableAutoLogin;
@property (nonatomic, retain) NSString * autoLogin;
@property (nonatomic, retain) NSNumber * smartExpenseEnabledOnReports;
@property (nonatomic, retain) NSString * twitterUrl;
@property (nonatomic, retain) NSNumber * isTripItLinked;
@property (nonatomic, retain) NSDate * dateRatedApp;
@property (nonatomic, retain) NSString * uri;
@property (nonatomic, retain) NSNumber * showIAd;
@property (nonatomic, retain) NSString * uriNonSSL;
@property (nonatomic, retain) NSString * mi;
@property (nonatomic, retain) NSString * roles;
@property (nonatomic, retain) NSString * RowKey;
@property (nonatomic, retain) NSNumber * rememberUser;
@property (nonatomic, retain) NSString * requestAppRating;
@property (nonatomic, retain) NSString * email;
@property (nonatomic, retain) NSString * companyName;
@property (nonatomic, retain) NSString * firstName;
@property (nonatomic, retain) NSString * backUpReceiptFilePath;
@property (nonatomic, retain) NSString * serverAddress;
@property (nonatomic, retain) NSString * showTwitter;
@property (nonatomic, retain) NSString * lastName;
@property (nonatomic, retain) NSString * enableTouchID;

@end

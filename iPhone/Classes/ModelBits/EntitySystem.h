//
//  EntitySystem.h
//  ConcurMobile
//
//  Created by  on 4/25/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntitySystem : NSManagedObject

@property (nonatomic, strong) NSNumber * showWhatsNew;
@property (nonatomic, strong) NSDate * timeLastGoodRequest;
@property (nonatomic, strong) NSString * fbUserId;
@property (nonatomic, strong) NSString * lastSessionID;
@property (nonatomic, strong) NSString * fbUserName;
@property (nonatomic, strong) NSString * fbLastName;
@property (nonatomic, strong) NSString * fbEmail;
@property (nonatomic, strong) NSNumber * isOffline;
@property (nonatomic, strong) NSNumber * useFacebook;
@property (nonatomic, strong) NSString * previousVersion;
@property (nonatomic, strong) NSString * topViewName;
@property (nonatomic, strong) NSString * sessionId;
@property (nonatomic, strong) NSString * lastViewName;
@property (nonatomic, strong) NSString * lastViewDataName;
@property (nonatomic, strong) NSString * fbBirthDate;
@property (nonatomic, strong) NSString * productOffering;
@property (nonatomic, strong) NSString * lastViewKey;
@property (nonatomic, strong) NSNumber * doReceiptMigrate;
@property (nonatomic, strong) NSNumber * debug;
@property (nonatomic, strong) NSString * fbFirstName;
@property (nonatomic, strong) NSString * crnCode;
@property (nonatomic, strong) NSString * expenseCtryCode;
@property (nonatomic, strong) NSString * receiptsPath;
@property (nonatomic, strong) NSNumber * timeOut;
@property (nonatomic, strong) NSString * productLine;
@property (nonatomic, strong) NSString * currentVersion;

@end

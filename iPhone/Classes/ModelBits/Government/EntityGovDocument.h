//
//  EntityGovDocument.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityGovDocument : NSManagedObject

@property (nonatomic, retain) NSString * docType;
@property (nonatomic, retain) NSString * travelerId;
@property (nonatomic, retain) NSString * approveLabel;
@property (nonatomic, retain) NSString * docTypeLabel;
@property (nonatomic, retain) NSString * docName;
@property (nonatomic, retain) NSString * gtmDocType;
@property (nonatomic, retain) NSString * travelerName;
@property (nonatomic, retain) NSNumber * needsStamping;
@property (nonatomic, retain) NSDate * tripEndDate;
@property (nonatomic, retain) NSDecimalNumber * totalExpCost;
@property (nonatomic, retain) NSDate * tripBeginDate;
@property (nonatomic, retain) NSString * purposeCode;
@property (nonatomic, retain) NSNumber * authForVch;

@end

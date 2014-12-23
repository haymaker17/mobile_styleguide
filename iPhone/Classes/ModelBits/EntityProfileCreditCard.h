//
//  EntityProfileCreditCard.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityProfile;

@interface EntityProfileCreditCard : NSManagedObject

@property (nonatomic, retain) NSString * number;
@property (nonatomic, retain) NSDate * expDate;
@property (nonatomic, retain) NSString * cvsNo;
@property (nonatomic, retain) NSString * billingAddress;
@property (nonatomic, retain) NSString * type;
@property (nonatomic, retain) EntityProfile *relProfile;

@end

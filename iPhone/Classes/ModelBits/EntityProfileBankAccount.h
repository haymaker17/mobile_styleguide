//
//  EntityProfileBankAccount.h
//  ConcurMobile
//
//  Created by Ray Chi on 12/2/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityProfile;

@interface EntityProfileBankAccount : NSManagedObject

@property (nonatomic, retain) NSString * acountNo;
@property (nonatomic, retain) NSString * routingNo;
@property (nonatomic, retain) NSString * type;
@property (nonatomic, retain) NSString * activeStatus;
@property (nonatomic, retain) EntityProfile *relProfile;

@end

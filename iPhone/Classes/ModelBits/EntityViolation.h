//
//  EntityViolation.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 16/07/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityTrip;

@interface EntityViolation : NSManagedObject

@property (nonatomic, retain) NSString * rule;
@property (nonatomic, retain) NSString * reason;
@property (nonatomic, retain) NSString * comments;
@property (nonatomic, retain) NSString * cost;
@property (nonatomic, retain) NSString * costAdditionalInfo;
@property (nonatomic, retain) NSString * type;
@property (nonatomic, retain) EntityTrip *relTrip;

@end

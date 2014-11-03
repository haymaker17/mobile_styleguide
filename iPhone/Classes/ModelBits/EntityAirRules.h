//
//  EntityAirRules.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityAirFilter, EntityAirFilterSummary;

@interface EntityAirRules : NSManagedObject

@property (nonatomic, strong) NSString * fareId;
@property (nonatomic, strong) NSString * exceptionMessage;
@property (nonatomic, strong) NSNumber * exceptionLevel;
@property (nonatomic, strong) EntityAirFilterSummary *AirFilterSummary;
@property (nonatomic, strong) EntityAirFilter *AirFilter;

@end

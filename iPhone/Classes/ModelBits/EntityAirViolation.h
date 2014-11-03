//
//  EntityAirViolation.h
//  ConcurMobile
//
//  Created by ernest cho on 8/27/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityAirFilter, EntityAirFilterSummary;

@interface EntityAirViolation : NSManagedObject

@property (nonatomic, strong) NSString * fareId;
@property (nonatomic, strong) NSString * code;
@property (nonatomic, strong) NSNumber * enforcementLevel;
@property (nonatomic, strong) NSString * message;
@property (nonatomic, strong) NSString * violationType;
@property (nonatomic, strong) EntityAirFilterSummary *relAirViolationCurrent;
@property (nonatomic, strong) EntityAirFilter *relAirFilter;
@property (nonatomic, strong) EntityAirFilterSummary *relAirFilterSummary;

@end

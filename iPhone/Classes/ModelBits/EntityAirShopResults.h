//
//  EntityAirShopResults.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 4/23/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityAirShopResults : NSManagedObject

@property (nonatomic, strong) NSString * pref;
@property (nonatomic, strong) NSNumber * numStops;
@property (nonatomic, strong) NSString * rateType;
@property (nonatomic, strong) NSString * airlineName;
@property (nonatomic, strong) NSNumber * numChoices;
@property (nonatomic, strong) NSString * crnCode;
@property (nonatomic, strong) NSNumber * lowestCost;
@property (nonatomic, strong) NSNumber * travelPoints;
@property (nonatomic, strong) NSString * airline;

@end

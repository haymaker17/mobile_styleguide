//
//  AirlineEntry.h
//  ConcurMobile
//
//  Created by Paul Kramer on 8/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface AirlineEntry : NSObject

@property (strong, nonatomic) NSString *airline;
@property (strong, nonatomic) NSString *pref;
@property (strong, nonatomic) NSString *crnCode;
@property (strong, nonatomic) NSNumber *lowestCost;
@property (strong, nonatomic) NSNumber *numChoices;
@property (strong, nonatomic) NSNumber *numStops;
@property (strong, nonatomic) NSString *rateType;
@property (strong, nonatomic) NSNumber *travelPoints;

@end

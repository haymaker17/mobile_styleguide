//
//  CarDetailData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 2/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CarDetailData : NSObject {
	NSString *carKey, *criteriaName, *distanceToDate, *isPreferred, *vehicleId;
	NSMutableArray	*aCarRateTypes;
	NSInteger odometerStart;

}

@property (strong, nonatomic) NSString *carKey;
@property (strong, nonatomic) NSString *criteriaName;
@property (strong, nonatomic) NSString *distanceToDate;
@property (strong, nonatomic) NSString *isPreferred;
@property (strong, nonatomic) NSString *vehicleId;
@property (strong, nonatomic) NSMutableArray	*aCarRateTypes;
@property NSInteger                     odometerStart;
@end

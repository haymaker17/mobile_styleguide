//
//  CarRateTypeData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 2/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CarRateData.h"


@interface CarRateTypeData : NSObject {
	NSString *lowerLimit, *rateType, *upperLimit;
	NSMutableArray			*aCarRateKeys;
	NSMutableDictionary		*dictCarRates;
	int						iLower, iUpper;

}

@property (strong, nonatomic) NSString					*lowerLimit;
@property (strong, nonatomic) NSString					*rateType;
@property (strong, nonatomic) NSString					*upperLimit;
@property (strong, nonatomic) NSMutableArray			*aCarRateKeys;
@property (strong, nonatomic) NSMutableDictionary		*dictCarRates;
@property int iLower;
@property int iUpper;

-(int) distanceInRateFrom:(int)start to:(int)end;
-(CarRateData *)rateForDate:(NSDate*)date;

@end

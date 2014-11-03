//
//  CarConfigData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CarDetailData.h"
#import "CarRateData.h"

@interface CarConfigData : NSObject {
	NSString				*canCreateExp, *carcfgKey, *companyOrPersonal, *configType, *crnKey, *ctryCode, *ctryDistanceUnitCode, *crnCode;
	NSMutableArray			*aCarRateKeys, *aCarDetailKeys;
	NSMutableDictionary		*dictCarRates, *dictCarDetails;
	BOOL					isPersonal;
}
@property BOOL isPersonal;
@property (strong, nonatomic) NSString				*canCreateExp;
@property (strong, nonatomic) NSString				*carcfgKey;
@property (strong, nonatomic) NSString				*companyOrPersonal;
@property (strong, nonatomic) NSString				*configType;
@property (strong, nonatomic) NSString				*crnKey;
@property (strong, nonatomic) NSString				*crnCode;
@property (strong, nonatomic) NSString				*ctryCode;
@property (strong, nonatomic) NSString				*ctryDistanceUnitCode;
@property (strong, nonatomic) NSMutableArray		*aCarRateKeys;
@property (strong, nonatomic) NSMutableDictionary	*dictCarRates;

@property (strong, nonatomic) NSMutableArray		*aCarDetailKeys;
@property (strong, nonatomic) NSMutableDictionary	*dictCarDetails;



- (CarDetailData*) findDetailForPreferredOrKey:(NSString*)key;
- (CarRateData *) findRateForDate:(NSDate *)date;

@end

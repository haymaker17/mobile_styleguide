//
//  CarRatesData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponder.h"
#import "Msg.h"
#import "CarConfigData.h"
#import "CarRateData.h"
#import "DataConstants.h"
#import "CarDetailData.h"
#import "CarRateTypeData.h"

@interface CarRatesData : MsgResponderCommon {
	NSMutableDictionary		*items;
	NSMutableArray			*keys;
	CarConfigData			*obj;
	CarRateData				*carRate;
	CarDetailData			*carDetail;
	CarRateTypeData			*carRateType;
}

@property (nonatomic, strong) NSMutableDictionary		*items;
@property (nonatomic, strong) CarConfigData				*obj;
@property (nonatomic, strong) NSMutableArray			*keys;
@property (nonatomic, strong) CarRateData				*carRate;
@property (nonatomic, strong) CarDetailData				*carDetail;
@property (nonatomic, strong) CarRateTypeData			*carRateType;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(void) flushData;

-(float) fetchRate:(NSDate *)date isPersonal:(BOOL)isPersonal isPersonalPartOfBusiness:(BOOL)isPersonalPartOfBusiness distance:(NSString *)distance 
            carKey:(NSString *)carKey ctryCode:(NSString *)ctryCode numPassengers:(NSString *)numPassengers distanceToDate:(int) distanceToDate;

//-(float) fetchRate:(NSDate *)date isPersonal:(BOOL)isPersonal;
-(NSString *) fetchDistanceUnit:(BOOL)isPersonal ctryCode:(NSString *)ctryCode;
//-(CarRateData *) fetchCarRate:(NSDate *)date isPersonal:(BOOL)isPersonal;
-(NSString *) fetchDistanceUnitAndRate:(BOOL)isPersonal date:(NSDate *)date ctryCode:(NSString *)ctryCode  distance:(NSString *)distance carKey:(NSString *)carKey;

-(CarRateData *) fetchCarRate:(NSDate *)date isPersonal:(BOOL)isPersonal distance:(NSString *)distance carKey:(NSString *)carKey ctryCode:(NSString *)ctryCode;

// DMB - add
/*!
 fetchCarConfig
 scroll round carConfigData until we find a carKey match
 NOTE Personal Car PER_ONE rate, this has a carKey of -1
 */
-(CarConfigData *) fetchCarConfig: (NSString *) carKey;
/*!
 fetchCarDetail
 scroll round carConfigData until we find a carKey match
 NOTE Personal Car PER_ONE rate, this has a carKey of -1
 */
-(CarDetailData *) fetchCarDetail: (NSString *) carKey;
/*!
 fetchCarDetailDefault
 uses fetchCarDetailsOrdered, return the default car detail based on the following criteria.
 only 1 car.
 only 1 is preferred car details record
 Personal car will act as if the is preferred is set to Y
 */
-(CarDetailData *) fetchCarDetailDefault;
/*!
 fetchCarDetailsOrdered
 uses fetchCarDetails and orders by is preferred / vehicle id
 */
-(NSArray *) fetchCarDetailsOrdered;
/*!
 fetchCarDetails
 fetch active personal, company car details & include personal car (PER_ONE rate) if applicable
 */
-(NSMutableDictionary *) fetchCarDetails;




-(BOOL) isPersonalVariable:(NSString *)ctryCode;
-(NSMutableDictionary *) fetchPersonalCarDetails:(NSString *)ctryCode;
-(NSMutableDictionary *) fetchCompanyCarDetails:(NSString *)ctryCode;
-(CarDetailData *) fetchPreferredPersonalCarDetail:(NSString *)ctryCode;
-(CarDetailData *) fetchCompanyCarDetail:(NSString *)carKey;

-(NSString *) fetchCarReimbursementRates:(NSDate *)date isPersonal:(BOOL)isPersonal distance:(NSString *)distance carKey:(NSString *)carKey ctryCode:(NSString *)ctryCode;
-(BOOL) hasAnyPersonalsWithRates:(NSString*) crnCode;
-(CarConfigData *) fetchPersonalCarConfig;
-(BOOL) hasAnyCompanyCarWithRates:(NSString*) crnCode;

/**
 * Screw it, this hack is used to fix issues with removing RootVC
 * MOB-17609 Missing car mileage expense types
 * Returns the last car rates object downloaed, this can be nil
 */
+ (CarRatesData *)lastCarRatesDataDownloaded;

@end


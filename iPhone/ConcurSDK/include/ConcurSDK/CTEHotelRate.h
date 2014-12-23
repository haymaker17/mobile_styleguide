//
//  CTEHotelRate.h
//  ConcurSDK
//
//  Created by Sally Yan on 7/16/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "CTECreditCard.h"
#import "CTEHotelCancellationPolicy.h"
#import "CTEHotelReserveConfirmation.h"
#import "CTEError.h"
#import "CTEHotel.h"
#import "CTEHotelViolation.h"

@interface CTEHotelRate : NSObject

@property (nonatomic, readonly, strong) NSString *rateID;

@property (nonatomic, readonly, strong) NSString *totalAmount;
@property (nonatomic, readonly, strong) NSString *dailyAmount;
@property (nonatomic, readonly, strong) NSString *currency;
@property (nonatomic, readonly, strong) NSString *rateCode;
@property (nonatomic, readonly, strong) NSString *roomType;
@property (nonatomic, readonly, strong) NSString *roomDescription;
@property (nonatomic, readonly, strong) NSString *guaranteeSurcharge;
@property (nonatomic, readonly, assign) BOOL      hasCancelPolicy;
@property (nonatomic, readonly, assign) BOOL      hasMealPlan;

// most of the time you only need the most severe violation
// be aware that there is an odd case where CTEHotelBookingAllowed can be more severe than CTEHotelBookingNotAllowed
@property (nonatomic, readonly, strong) CTEHotelViolation *mostSevereViolation;

// all the violations
@property (nonatomic, readonly, strong) NSArray *violations;

// back pointer to parent hotel
@property (nonatomic, readonly, weak) CTEHotel *hotel;

-(id)initWithRatesDictionary:(NSDictionary *)response hotel:(CTEHotel *)hotel;

- (void)reserveWithCreditCard:(CTECreditCard *)selectedCreditCard
             violationReasons:(NSArray *)violationsReasons
                    addToTrip:(NSString *)tripId
                      success:(void (^)(CTEHotelReserveConfirmation *reservation))success
                      failure:(void (^)(CTEError *error))failure;

// fetches presell options
// this allows the UI to decide when to lazy load the pre-sell options
- (void)presellOptionsWithCompletionBlock:(void (^)(NSArray *creditCards, NSString *cancellationPolicy))completion;

// need for unit tests
@property (nonatomic, readonly, strong) NSString *sellOptionsURL;

@end
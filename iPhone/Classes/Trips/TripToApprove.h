//
//  TripToApprove.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 02/05/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface TripToApprove : NSObject

@property (nonatomic, strong) NSDate *approveByDate;
@property (nonatomic, strong) NSString *itinLocator;
@property (nonatomic, strong) NSString *travelerCompanyId;
@property (nonatomic, strong) NSString *travelerName;
@property (nonatomic, strong) NSString *travelerUserId;
@property (nonatomic, strong) NSString *tripName;
@property (nonatomic, strong) NSString *totalTripCostCrnCode;
@property (nonatomic, strong) NSNumber *totalTripCost;
//@property (nonatomic, strong) NSDate *bookedDate;
//@property (nonatomic, strong) NSDate *endDate;
//@property (nonatomic, strong) NSDate *lastTicketDate;
//@property (nonatomic, strong) NSDate *startDate;
//@property (nonatomic, strong) NSString *recordLocator;
//@property (nonatomic, strong) NSString *tripId;

@end

//
//  AmtrakShop.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface AmtrakShop : NSObject {
	NSString		*arrivalStation, *arrivalStationTimeZoneId, *classOfTravel, *departureDateTime, *departureStation, *departureStationTimeZoneId, *directOnly, *numberOfPassengers, *refundableOnly, *returnDateTime;
}

@property (strong, nonatomic) NSString		*arrivalStation;
@property (strong, nonatomic) NSString		*arrivalStationTimeZoneId;
@property (strong, nonatomic) NSString		*classOfTravel;
@property (strong, nonatomic) NSString		*departureDateTime;
@property (strong, nonatomic) NSString		*departureStation;
@property (strong, nonatomic) NSString		*departureStationTimeZoneId;
@property (strong, nonatomic) NSString		*directOnly;
@property (strong, nonatomic) NSString		*numberOfPassengers;
@property (strong, nonatomic) NSString		*refundableOnly;
@property (strong, nonatomic) NSString		*returnDateTime;
@end

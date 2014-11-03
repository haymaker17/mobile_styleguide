//
//  TrainStationData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface TrainStationData : NSObject 
{
	NSString			*city, *countryCode, *iataCode, *stationState, *stationCode, *stationName, *timeZoneName, *stationPhone, *stationZip;
	NSString			*stationAddress;
	float				waitTime;
}

@property (strong, nonatomic) NSString			*city;
@property (strong, nonatomic) NSString			*countryCode;
@property (strong, nonatomic) NSString			*iataCode;
@property (strong, nonatomic) NSString			*stationState;
@property (strong, nonatomic) NSString			*stationCode;
@property (strong, nonatomic) NSString			*stationName;
@property (strong, nonatomic) NSString			*timeZoneName;
@property (strong, nonatomic) NSString			*stationAddress;
@property (strong, nonatomic) NSString			*stationPhone;
@property (strong, nonatomic) NSString			*stationZip;
@property float				waitTime;

@end
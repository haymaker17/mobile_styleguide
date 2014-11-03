//
//  RailChoiceTrainData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface RailChoiceTrainData : NSObject {

	int						leg;
	NSString				*carrier, *fltNum, *depDateTime, *arrDateTime, *aircraftCode, *meals, *bic, *fltClass, *depAirp, *arrAirp, *flightTime;
}

@property int							leg;
@property (strong, nonatomic) NSString	*carrier;
@property (strong, nonatomic) NSString	*fltNum;
@property (strong, nonatomic) NSString	*depDateTime;
@property (strong, nonatomic) NSString	*arrDateTime;
@property (strong, nonatomic) NSString	*aircraftCode;
@property (strong, nonatomic) NSString	*meals;
@property (strong, nonatomic) NSString	*bic;
@property (strong, nonatomic) NSString	*fltClass;
@property (strong, nonatomic) NSString	*depAirp;
@property (strong, nonatomic) NSString	*arrAirp;
@property (strong, nonatomic) NSString	*flightTime;

@end

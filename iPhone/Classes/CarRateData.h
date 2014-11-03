//
//  CarRateData.h
//  ConcurMobile
//
//  Created by Paul Kramer on 1/20/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CarRateData : NSObject {
	NSString	*key, *rate, *startDate;
	NSDate		*dateStart;
}

@property (strong, nonatomic) NSString	*key;
@property (strong, nonatomic) NSString	*rate;
@property (strong, nonatomic) NSString	*startDate;
@property (strong, nonatomic) NSDate	*dateStart;

@end

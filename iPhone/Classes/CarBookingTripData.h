//
//  CarBookingTripData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface CarBookingTripData : NSObject
{
	NSString *tripKey;
	NSString *clientLocator;
	NSString *recordLocator;
}

@property (nonatomic, strong) NSString*	tripKey;
@property (nonatomic, strong) NSString*	clientLocator;
@property (nonatomic, strong) NSString*	recordLocator;

@end

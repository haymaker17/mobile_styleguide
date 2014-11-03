//
//  HotelReservationResponse.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface HotelReservationResponse : NSObject
{
	NSString	*errorMessage;
	NSString	*status;
	NSString	*recordLocator;
    NSString    *authorizationNumber;
}

@property (nonatomic, strong) NSString	*errorMessage;
@property (nonatomic, strong) NSString	*status;
@property (nonatomic, strong) NSString	*recordLocator;
@property (nonatomic, strong) NSString	*authorizationNumber;
@property (nonatomic, strong) NSString  *itinLocator;

@end

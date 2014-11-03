//
//  HotelViolation.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/11/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface HotelViolation : NSObject
{
	NSString	*message;
	NSString	*code;
    NSString    *enforcementLevel;
    NSString    *violationType;
}

@property (nonatomic, strong) NSString	*message;
@property (nonatomic, strong) NSString	*code;
@property (nonatomic, strong) NSString  *enforcementLevel;
@property (nonatomic, strong) NSString  *violationType;

@end

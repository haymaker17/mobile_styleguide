//
//  HotelDetail.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface HotelDetail : NSObject
{
	NSString	*name;
	NSString	*description;
}

@property (nonatomic, strong) NSString	*name;
@property (nonatomic, strong) NSString	*description;

@end

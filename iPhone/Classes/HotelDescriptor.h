//
//  HotelDescriptor.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/30/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class HotelResult;

@interface HotelDescriptor : NSObject
{
	NSUInteger	parentHotelIndex;
	HotelResult	*detail;
}

@property (nonatomic) NSUInteger			parentHotelIndex;
@property (nonatomic, strong) HotelResult*	detail;

@end

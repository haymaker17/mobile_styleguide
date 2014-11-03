//
//  RoomResult.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/24/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface RoomResult : NSObject
{
	NSString		*currencyCode;
	NSString		*rate;
	NSString		*summary;
	NSString		*bicCode;
	NSString		*sellSource;
	NSMutableArray	*violations;
	NSString		*violationReasonCode;
	NSString		*violationJustification;
}

@property (nonatomic, strong) NSString			*currencyCode;
@property (nonatomic, strong) NSString			*rate;
@property (nonatomic, strong) NSString			*summary;
@property (nonatomic, strong) NSString			*bicCode;
@property (nonatomic, strong) NSString			*sellSource;
@property (nonatomic, strong) NSMutableArray	*violations;
@property (nonatomic, strong) NSString			*violationReasonCode;
@property (nonatomic, strong) NSString			*violationJustification;
@property (weak, nonatomic, readonly) NSString		*violationReason;


@end

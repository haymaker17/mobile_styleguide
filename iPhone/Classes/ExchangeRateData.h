//
//  ExchangeRateData.h
//  ConcurMobile
//
//  Created by yiwen on 12/10/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ExSystem.h"

@interface ExchangeRateData : MsgResponderCommon 
{
	NSString	*fromCrnCode;
	NSString	*toCrnCode;
	NSString	*forDate;	// in yyyy-mm-dd format
	double		rate;
	NSString	*status;
}

@property (nonatomic, strong) NSString		*fromCrnCode;
@property (nonatomic, strong) NSString		*toCrnCode;
@property (nonatomic, strong) NSString		*forDate;
@property (nonatomic, strong) NSString		*status;

@property double rate;

- (void)encodeWithCoder:(NSCoder *)coder;
- (id)initWithCoder:(NSCoder *)coder;

-(Msg *)newMsg: (NSMutableDictionary *)parameterBag;
-(NSString *)getMsgIdKey;

@end

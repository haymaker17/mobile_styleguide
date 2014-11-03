//
//  ExCarDistanceToDateData.h
//  ConcurMobile
//
//  Created by yiwen on 3/17/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"

@interface ExCarDistanceToDateData : MsgResponderCommon 
{
	NSString				*carKey;
	NSString				*tranDate;  // in yyyy-mm-dd format
	NSString				*excludeRpeKey;
	NSString				*distanceToDate;
}

@property (nonatomic, strong) NSString		*carKey;
@property (nonatomic, strong) NSString		*tranDate;
@property (nonatomic, strong) NSString		*excludeRpeKey;
@property (nonatomic, strong) NSString		*distanceToDate;


-(Msg *)newMsg: (NSMutableDictionary *)parameterBag;
-(NSString *)getMsgIdKey;


@end

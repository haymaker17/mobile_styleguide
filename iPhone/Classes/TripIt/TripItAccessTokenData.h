//
//  TripItAccessTokenData.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 3/30/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"

@class MsgControl;
@class Msg;

@interface TripItAccessTokenData : MsgResponderCommon
{
}

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end

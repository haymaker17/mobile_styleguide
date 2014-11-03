//
//  TripItLink.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 5/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ActionStatus.h"

@interface TripItLink : MsgResponderCommon
{
	ActionStatus    *linkStatus;
}

@property (nonatomic, strong) ActionStatus  *linkStatus;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(NSString *) makeXMLBody:(NSMutableDictionary*)pBag;

@end

//
//  MobileExpneseDelete.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 4/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Msg.h"
#import "MsgResponder.h"


@interface MobileExpenseDelete : MsgResponder
{
	    
	NSString                *returnStatus;
	NSMutableDictionary		*keysToKill, *returnFailures, *returnFailure;
}

@property (nonatomic, strong) NSString					*returnStatus;
@property (nonatomic, strong) NSMutableDictionary		*keysToKill;
@property (nonatomic, strong) NSMutableDictionary		*returnFailures;
@property (nonatomic, strong) NSMutableDictionary		*returnFailure;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;
-(id)init;
-(void) flushData;

@end

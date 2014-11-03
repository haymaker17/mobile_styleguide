//
//  ValidateSessionData.h
//  ConcurMobile
//
//  Created by yiwen on 10/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"

@interface ValidateSessionData : MsgResponderCommon 
{
	NSString				*isInElement;
	NSMutableDictionary		*dict;
}
@property (nonatomic, strong) NSMutableDictionary		*dict;
@property (nonatomic, strong) NSString                  *isInElement;

-(id)init;

-(void) flushData;



@end

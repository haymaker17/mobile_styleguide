//
//  PostMessageManager.h
//  ConcurMobile
//
//  Created by yiwen on 10/27/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//
#import <Foundation/Foundation.h>
#import "BaseManager.h"
#import "EntityPostMessage.h"
#import "EntityPostMessageBody.h"
@interface PostMessageManager : BaseManager 
{
    
}

// Need to add sequence number or time stamp to post message

+(PostMessageManager*)sharedInstance;
-(EntityPostMessage *) makeNew;
-(EntityPostMessageBody *) makeNewBody;

-(void) clearAll;
-(EntityPostMessage *) getMessageByUUID:(NSString *)uuid;
-(void) deleteMessage:(NSString*)uuid;

@end

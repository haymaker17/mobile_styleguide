//
//  PostMsgInfo.m
//  ConcurMobile
//
//  Created by yiwen on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "PostMsgInfo.h"

@implementation PostMsgInfo

@synthesize msg;
@synthesize msgCtrl;
@synthesize reqCtrl;
@synthesize msgId;

+ (NSString* ) getUUID
{
    return [[NSUUID UUID] UUIDString];
}

- (id) init: (Msg*) message messageControl:(MsgControl*)msgControl
{
    self = [super init];
    if (self)
    {
        self.msg = message;
        self.msgId = message.uuid; //[PostMsgInfo getUUID];
        self.msgCtrl = msgControl;
    }
    return self;
}

- (void) send
{
    if (reqCtrl == nil)
    {
//        [[MCLogging getInstance] log:[NSString stringWithFormat:@"MsgControl::send(%@) fetching data", self.msg.idKey] Level:MC_LOG_DEBU];
        RequestController* reqCtl = [RequestController alloc];	//make an instance of the request controller to fetch the data        
        self.reqCtrl = reqCtl; 
        // TODO test whether should use initPostMsg instead
        [self.reqCtrl init:self.msg MessageControl: self.msgCtrl];					//init the request controller, which will start the async data get
    } else {
        [self.reqCtrl send];
    }
    
}

// Used by the queue to locate the message when results comes back
- (BOOL) owns:(RequestController *) target
{
    if (self.reqCtrl == target)
        return TRUE;
    return FALSE;
}

- (void)encodeWithCoder:(NSCoder *)coder {
//    [super encodeWithCoder:coder];
    [coder encodeObject:msg forKey:@"Msg"];
    [coder encodeObject:msgId forKey:@"MsgId"];
}

- (id)initWithCoder:(NSCoder *)coder {
//    self = [super initWithCoder:coder];
    self.msg = [coder decodeObjectForKey:@"Msg"];
    self.msgId = [coder decodeObjectForKey:@"MsgId"];
    return self;
}


@end

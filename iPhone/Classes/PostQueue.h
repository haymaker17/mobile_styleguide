//
//  PostQueue.h
//  ConcurMobile
//
//  Created by yiwen on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Msg.h"
#import "MsgControl.h"
#import "RequestController.h"

@interface PostQueue : NSObject {
	NSMutableArray		*postMsgs;
}

@property (nonatomic, strong) NSMutableArray *postMsgs; 

+ (PostQueue*) getInstance;

- (id) initQueue;
- (void) registerPostMsg: (Msg*) msg messageControl:(MsgControl*) msgCtrl;
- (void) finishPostMsg: (RequestController*) reqCtrl result:(NSString*) status;
- (void) restartPostQueue: (MsgControl*) msgCtrl; 
- (int)  count;

// Save upon app exit
//- (BOOL) persistQueue;
// Restore upon app start
//+ (PostQueue*) restoreQueue;

- (void)encodeWithCoder:(NSCoder *)coder;
- (id)initWithCoder:(NSCoder *)coder;
@end

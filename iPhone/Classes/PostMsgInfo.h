//
//  PostMsgInfo.h
//  ConcurMobile
//
//  Created by yiwen on 3/31/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "Msg.h"
#import "MsgControl.h"
#import "RequestController.h"

@interface PostMsgInfo : NSObject {
    // TODO - add timestamp
    Msg                 * msg;
    NSString            * msgId;
    RequestController   * reqCtrl;
    MsgControl          * msgCtrl;
}

@property (nonatomic, strong) Msg *msg; 
@property (nonatomic, strong) NSString * msgId;
@property (nonatomic, strong) MsgControl *msgCtrl; 
@property (nonatomic, strong) RequestController *reqCtrl; 

- (id) init: (Msg*) msg messageControl: (MsgControl*) msgControl;
- (void) send;
- (BOOL) owns:(RequestController *) reqCtrl;

- (void)encodeWithCoder:(NSCoder *)coder;
- (id)initWithCoder:(NSCoder *)coder;

+ (NSString* ) getUUID;

@end

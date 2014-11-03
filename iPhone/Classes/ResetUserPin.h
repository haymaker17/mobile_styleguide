//
//  ResetUserPin.h
//  ConcurMobile
//
//  Created by AJ Cram on 6/28/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

extern NSString* const RESET_USER_PIN_CLIENT_KEY;
extern NSString* const RESET_USER_PIN_LOGIN_ID;
extern NSString* const RESET_USER_PIN_SERVER_KEY;

@interface ResetUserPin : MsgResponderCommon
{
    NSString        *status;
    NSString        *userID;
    NSString        *PinMinLength;
    NSString        *requiredMixedCase;
    NSString        *requiresNonAlphanum;
    NSString        *errMsg;
    ActionStatus    *actionStatus;
}
@property (nonatomic, strong) NSString      *status;
@property (nonatomic, strong) NSString      *userID;
@property (nonatomic, strong) NSString      *PinMinLength;
@property (nonatomic, strong) ActionStatus *actionStatus;
@property (nonatomic, strong) NSString      *requiredMixedCase;
@property (nonatomic, strong) NSString      *requiresNonAlphanum;
@property (nonatomic, strong) NSString      *errMsg;


-(Msg *) newMsg:(NSMutableDictionary *)parameterBag; 

@end

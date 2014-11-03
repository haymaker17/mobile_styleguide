//
//  AppendReceiptData.h
//  ConcurMobile
//
//  Created by Paul Schmidt on 12/7/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "MsgResponderCommon.h"
#import "ActionStatus.h"

@interface AppendReceiptData : MsgResponderCommon
{
    NSString				*fromReceiptImageId;
    NSString				*toReceiptImageId;
    ActionStatus            *actionStatus;
}
@property (nonatomic,strong) NSString       *fromReceiptImageId;
@property (nonatomic,strong) NSString       *toReceiptImageId;
@property (nonatomic, strong) ActionStatus  *actionStatus;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end

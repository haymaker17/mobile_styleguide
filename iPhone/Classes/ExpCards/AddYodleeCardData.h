//
//  AddYodleeCardData.h
//  ConcurMobile
//
//  Created by yiwen on 11/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//


#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "ActionStatus.h"
#import "PersonalCard.h"

@interface AddYodleeCardData : MsgResponderCommon { 
}

@property (nonatomic, strong) NSString                  *contentServiceId;
@property (nonatomic, strong) NSArray                   *fields;
@property (nonatomic, strong) ActionStatus				*status;
@property (nonatomic, strong) PersonalCard				*card;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end

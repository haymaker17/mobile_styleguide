//
//  YodleeCardLoginFormData.h
//  ConcurMobile
//
//  Created by yiwen on 11/15/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//


#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "FormFieldData.h"

@interface YodleeCardLoginFormData : MsgResponderCommon {
    
}

@property (nonatomic, strong) NSString                  *contentServiceId;
@property (nonatomic, strong) NSMutableArray            *fields;
@property (nonatomic, strong) FormFieldData             *formField;

-(Msg *) newMsg:(NSMutableDictionary *)parameterBag;

@end

//
//  AttendeeSearchFieldsData.h
//  ConcurMobile
//
//  Created by yiwen on 10/13/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "MsgResponderCommon.h"
#import "FormFieldData.h"

@interface AttendeeSearchFieldsData : MsgResponderCommon 
{
    NSMutableDictionary     *forms;
    NSMutableArray          *fields;
    NSString                *atnTypeKey;
    FormFieldData           *formField;
}

@property (strong, nonatomic) NSString                  *atnTypeKey;
@property (strong, nonatomic) NSMutableDictionary       *forms;
@property (strong, nonatomic) NSMutableArray            *fields;
@property (strong, nonatomic) FormFieldData             *formField;

-(Msg *)newMsg: (NSMutableDictionary *)parameterBag;
-(NSString *)getMsgIdKey;


@end

//
//  GovExpenseSaveData.h
//  ConcurMobile
//
//  Created by ernest cho on 9/17/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface GovExpenseSaveData : MsgResponderCommon
{
    NSArray                 *fields;
    NSDictionary            *formAttributes;
    NSString                *expId;
}

@property (nonatomic, strong) NSArray                   *fields;
@property (nonatomic, strong) NSDictionary              *formAttributes;
@property (nonatomic, strong) NSString                  *expId;

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag;

-(NSString *) makeXMLBody;

@end

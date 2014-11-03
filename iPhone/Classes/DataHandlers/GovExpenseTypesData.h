//
//  GovExpenseTypesData.h
//  ConcurMobile
//
//  Created by ernest cho on 9/13/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@class GovExpenseType;

@interface GovExpenseTypesData : MsgResponderCommon
{
    GovExpenseType          *currentExpenseType;
    
    // key is the expense description
    NSMutableDictionary     *expenseTypes;
}

@property (nonatomic, strong) GovExpenseType            *currentExpenseType;
@property (nonatomic, strong) NSMutableDictionary       *expenseTypes;

-(id) init;
-(Msg*) newMsg:(NSMutableDictionary*)parameterBag;

-(NSArray*) getDescriptions;
-(GovExpenseType*) getExpenseFor:(NSString*)description;

@end

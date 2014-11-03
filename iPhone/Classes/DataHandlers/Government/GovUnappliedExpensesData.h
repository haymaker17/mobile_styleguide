//
//  GovUnappliedExpensesData.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 12/30/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "EntityGovExpense.h"

@interface GovUnappliedExpensesData : MsgResponderCommon
{
    EntityGovExpense        *currentExpense;
}

@property (nonatomic, strong) EntityGovExpense                  *currentExpense;
@property (nonatomic, strong, readonly) NSManagedObjectContext  *managedObjectContext;

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag;


@end

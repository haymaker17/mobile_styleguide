//
//  GovDeleteUnappliedExpenseData.h
//  ConcurMobile
//
//  Created by charlottef on 1/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MsgResponderCommon.h"
#import "ActionStatus.h"

@interface GovDeleteUnappliedExpenseData : MsgResponderCommon
{
    NSString        *ccExpId;
    ActionStatus    *status;
}

@property (nonatomic, strong) NSString      *ccExpId;
@property (strong, nonatomic) ActionStatus  *status;
@property (nonatomic, strong, readonly) NSManagedObjectContext *managedObjectContext;

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag;
-(NSString *) makeXMLBody;

@end
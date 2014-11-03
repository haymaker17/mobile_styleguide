//
//  GovDeleteExpFromDocData.h
//  ConcurMobile
//
//  Created by charlottef on 1/24/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MsgResponderCommon.h"
#import "ActionStatus.h"

@interface GovDeleteExpFromDocData : MsgResponderCommon
{
    NSString        *expenseId;
    NSString        *docName;
    NSString        *docType;
    ActionStatus    *status;
}

@property (nonatomic, strong) NSString      *expenseId;
@property (nonatomic, strong) NSString      *docName;
@property (nonatomic, strong) NSString      *docType;
@property (strong, nonatomic) ActionStatus  *status;

-(Msg*) newMsg:(NSMutableDictionary*)parameterBag;
-(NSString *) makeXMLBody;

@end

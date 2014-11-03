//
//  ConditionalFields.h
//  ConcurMobile
//
//  Created by Antonio Alwan on 12/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import "ConditionalFieldsActionList.h"

@interface ConditionalFieldsList : MsgResponder

// required for parser
@property (nonatomic, strong) ConditionalFieldsActionList  *conditionalFieldListData;

@end
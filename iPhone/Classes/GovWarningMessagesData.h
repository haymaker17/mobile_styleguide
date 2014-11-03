//
//  GovWarningMessagesData.h
//  ConcurMobile
//
//  Created by Shifan Wu on 1/26/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MsgResponderCommon.h"
#import "EntityWarningMessages.h"

@interface GovWarningMessagesData : MsgResponderCommon
{
    EntityWarningMessages               *warningMessages;
}

@property (strong, nonatomic) EntityWarningMessages             *warningMessages;
@property (strong, nonatomic) NSManagedObjectContext            *managedObjectContext;

@end

//
//  EntityPostMessageBody.h
//  ConcurMobile
//
//  Created by yiwen on 11/2/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityPostMessage;

@interface EntityPostMessageBody : NSManagedObject

@property (nonatomic, strong) NSData * data;
@property (nonatomic, strong) EntityPostMessage *relPostMessage;

@end

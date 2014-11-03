//
//  EntityChatterCommentEntry.h
//  ConcurMobile
//
//  Created by  on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import "EntityChatterAbstractEntry.h"

@class EntityChatterFeedEntry;

@interface EntityChatterCommentEntry : EntityChatterAbstractEntry

@property (nonatomic, strong) EntityChatterFeedEntry *feedEntry;

@end

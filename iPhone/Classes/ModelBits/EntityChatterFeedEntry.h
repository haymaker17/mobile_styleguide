//
//  EntityChatterFeedEntry.h
//  ConcurMobile
//
//  Created by  on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>
#import "EntityChatterAbstractEntry.h"

@class EntityChatterCommentEntry, EntityChatterFeed;

@interface EntityChatterFeedEntry : EntityChatterAbstractEntry

@property (nonatomic, strong) EntityChatterFeed *feed;
@property (nonatomic, strong) NSSet *comments;
@end

@interface EntityChatterFeedEntry (CoreDataGeneratedAccessors)

- (void)addCommentsObject:(EntityChatterCommentEntry *)value;
- (void)removeCommentsObject:(EntityChatterCommentEntry *)value;
- (void)addComments:(NSSet *)values;
- (void)removeComments:(NSSet *)values;
@end

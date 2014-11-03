//
//  EntityChatterFeed.h
//  ConcurMobile
//
//  Created by  on 8/1/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityChatterFeedEntry;

@interface EntityChatterFeed : NSManagedObject

@property (nonatomic, strong) NSString * label;
@property (nonatomic, strong) NSSet *entries;
@end

@interface EntityChatterFeed (CoreDataGeneratedAccessors)

- (void)addEntriesObject:(EntityChatterFeedEntry *)value;
- (void)removeEntriesObject:(EntityChatterFeedEntry *)value;
- (void)addEntries:(NSSet *)values;
- (void)removeEntries:(NSSet *)values;
@end

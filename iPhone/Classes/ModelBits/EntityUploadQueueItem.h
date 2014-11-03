//
//  EntityUploadQueueItem.h
//  ConcurMobile
//
//  Created by charlottef on 12/21/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityUploadQueueItem;

@interface EntityUploadQueueItem : NSManagedObject

@property (nonatomic, retain) NSString * entityInstanceId;
@property (nonatomic, retain) NSDate * creationDate;
@property (nonatomic, retain) NSString * uuid;
@property (nonatomic, retain) NSString * loginId;
@property (nonatomic, retain) NSString * entityTypeName;
@property (nonatomic, retain) NSSet *relRequiredBy;
@property (nonatomic, retain) NSSet *relRequires;
@end

@interface EntityUploadQueueItem (CoreDataGeneratedAccessors)

- (void)addRelRequiredByObject:(EntityUploadQueueItem *)value;
- (void)removeRelRequiredByObject:(EntityUploadQueueItem *)value;
- (void)addRelRequiredBy:(NSSet *)values;
- (void)removeRelRequiredBy:(NSSet *)values;

- (void)addRelRequiresObject:(EntityUploadQueueItem *)value;
- (void)removeRelRequiresObject:(EntityUploadQueueItem *)value;
- (void)addRelRequires:(NSSet *)values;
- (void)removeRelRequires:(NSSet *)values;

@end

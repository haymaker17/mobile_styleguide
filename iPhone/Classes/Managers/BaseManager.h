//
//  BaseManager.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/16/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface BaseManager : NSObject {
    NSManagedObjectContext      *_context;
}

@property (nonatomic, strong) NSManagedObjectContext *context;

-(void) saveIt:(NSManagedObject *) obj;
-(BOOL) hasAny:(NSString *) entityName;
-(NSManagedObject *) makeNew:(NSString *) entityName;
-(NSManagedObject *) fetchFirst:(NSString *) entityName;

-(NSArray *) fetchAll:(NSString *) entityName;
-(NSManagedObject *) fetchOrMake:(NSString *) entityName key:(NSString *)key;
//- (NSFetchedResultsController *)fetchedResultsController: (MobileViewController *) mvc entityName:(NSString *) entityName fetchedRC:(NSFetchedResultsController *) fetchedRC sectionNameKeyPath:(NSString *) sectionNameKeyPath;
-(void) deleteObj:(NSManagedObject *)obj;
-(void) deleteObj:(NSManagedObject *)obj withContext:(NSManagedObjectContext*) customContext;

+(BaseManager*)sharedInstance;
-(id)init;

// Fetch with condition
-(NSArray *) fetch:(NSString *) entityName withCondition:(NSPredicate*) pred withContext:(NSManagedObjectContext*) customContext;
-(NSManagedObject *) fetchFirst:(NSString *) entityName withCondition:(NSPredicate*) pred;

#pragma Utility APIs for all
+(NSManagedObject *) makeNew:(NSString *) entityName withContext:(NSManagedObjectContext*) customContext;
+(NSArray *) fetchAll:(NSString *) entityName withContext:(NSManagedObjectContext*) customContext;
+(BOOL) hasEntriesForEntityName:(NSString *)entityName withContext:(NSManagedObjectContext*) customContext;
+(void) deleteObject:(NSManagedObject*)obj withContext:(NSManagedObjectContext*) customContext;
+(void) deleteAll:(NSString *) entityName withContext:(NSManagedObjectContext*) context;

@end

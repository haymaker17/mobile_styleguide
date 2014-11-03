//
//  BaseManager.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/16/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "BaseManager.h"
#import "ConcurMobileAppDelegate.h"

static BaseManager *sharedInstance;

@implementation BaseManager
@synthesize context = _context;

+(BaseManager*)sharedInstance
{
	if (sharedInstance != nil) 
	{
		return sharedInstance;
	}
	else 
	{
		@synchronized (self)
		{
			if (sharedInstance == nil) 
			{
				sharedInstance = [[BaseManager alloc] init];
			}
		}
		return sharedInstance;
	}
}


-(id)init
{
    self = [super init];
	if (self)
	{
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.context = [ad managedObjectContext];
	}
    
	return self;
}



#pragma mark -
#pragma mark Expense Types default coredata
-(void) saveIt:(NSManagedObject *) obj
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    [[NSNotificationCenter defaultCenter] addObserver:ad
                                             selector:@selector(processNotification:)
                                                 name:NSManagedObjectContextDidSaveNotification
                                               object:self.context];
    
    NSError *error;
    if (![self.context save:&error])
        NSLog(@"Whoops, couldn't save object: %@", [error localizedDescription]);
    
    
    [[NSNotificationCenter defaultCenter] removeObserver:ad name:NSManagedObjectContextDidSaveNotification object:self.context];
}

-(BOOL) hasAny:(NSString *) entityName
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return YES;
    else
        return NO;
}

-(NSManagedObject *) fetchFirst:(NSString *) entityName
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
//    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(code = %@)", code];
//    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return nil;
}

// Pass nil pred to fetch All for the given context
-(NSArray *) fetch:(NSString *) entityName withCondition:(NSPredicate*) pred withContext:(NSManagedObjectContext*) customContext
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: customContext];
    [fetchRequest setEntity:entity];
    if (pred != nil)
        [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [customContext executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(NSManagedObject *) fetchFirst:(NSString *) entityName withCondition:(NSPredicate*) pred
{
    NSArray *aFetch = [self fetch:entityName withCondition:pred withContext:self.context];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return nil;
}


-(NSArray *) fetchAll:(NSString *) entityName
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
//    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"name" ascending:YES];
//    [fetchRequest setSortDescriptors:[NSArray arrayWithObject:sort]];
//    [sort release];

    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
    
}


-(NSManagedObject *) makeNew:(NSString *) entityName
{
    return [NSEntityDescription insertNewObjectForEntityForName:entityName inManagedObjectContext:self.context];
}


-(NSManagedObject *) fetchOrMake:(NSString *) entityName key:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(key = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return [self makeNew:entityName];
}


-(void) deleteObj:(NSManagedObject*)obj withContext:(NSManagedObjectContext*) customContext
{
    [customContext deleteObject:obj];
    NSError *error;
    if (![customContext save:&error]) {
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
    }
}

-(void) deleteObj:(NSManagedObject *)obj
{
    [self deleteObj:obj withContext:_context];
}

#pragma Utility APIs for all
+(NSManagedObject *) makeNew:(NSString *) entityName withContext:(NSManagedObjectContext*) customContext
{
    return [NSEntityDescription insertNewObjectForEntityForName:entityName inManagedObjectContext:customContext];
}

+(NSArray *) fetchAll:(NSString *) entityName withContext:(NSManagedObjectContext*) customContext
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: customContext];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [customContext executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

+(BOOL) hasEntriesForEntityName:(NSString *)entityName withContext:(NSManagedObjectContext*) customContext
{
    NSFetchRequest *request = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext:customContext];
    [request setEntity:entity];
    [request setFetchLimit:1];
    NSError *error = nil;
//    if (entity == nil)
//        return NO;
    NSArray *results = [customContext executeFetchRequest:request error:&error];
    if (!results) {
        NSLog(@"Fetch error: %@", error);
        abort();
    }
    if ([results count] == 0) {
        return NO;
    }
    return YES;
}

+(void) deleteObject:(NSManagedObject*)obj withContext:(NSManagedObjectContext*) customContext
{
    [customContext deleteObject:obj];
    NSError *error;
    if (![customContext save:&error]) {
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
    }
}

+(void) deleteAll:(NSString *) entityName withContext:(NSManagedObjectContext*) context
{
    NSArray* allObj = [self fetchAll:entityName withContext:context];
    for(NSManagedObject * obj in allObj)
        [self deleteObject:obj withContext:context];
}

@end

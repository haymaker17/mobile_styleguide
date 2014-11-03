//
//  AirViolationManager.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 1/4/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "AirViolationManager.h"

static AirViolationManager *sharedInstance;

@implementation AirViolationManager

@synthesize context = _context;
@synthesize entityName;

+(AirViolationManager*)sharedInstance
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
				sharedInstance = [[AirViolationManager alloc] init];
			}
		}
		return sharedInstance;
	}
}


-(AirViolationManager*)init
{
    self = [super init];
	if (self)
	{
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.context = [ad managedObjectContext];
        self.entityName = @"EntityAirViolation";
	}
    
	return self;
}

/**
 Init the manager with a given context
 @param incontext - private context if initiated from a xml parser
 @return new instance of object
 */
-(instancetype)initWithContext:(NSManagedObjectContext *)inContext
{
    self = [super init];
	if (self)
	{
        self.context = inContext;
        self.entityName = @"EntityAirViolation";
	}
    
	return self;
}




#pragma mark -
#pragma mark Expense Types default coredata
-(void) saveIt:(NSManagedObject *) obj
{
    NSError *error;
    if (![self.context save:&error])
        NSLog(@"Whoops, couldn't save object: %@", [error localizedDescription]);        
}

-(BOOL) hasAny
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

-(NSManagedObject *) fetchFirst
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return nil;
}

-(NSArray *) fetchAll
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
    
}


-(NSManagedObject *) makeNew
{
    return [NSEntityDescription insertNewObjectForEntityForName:entityName inManagedObjectContext:self.context];
}

-(NSManagedObject *) fetchOrMake:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return [self makeNew];
}

-(NSArray *) fetchByFareId:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"enforcementLevel" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(void) deleteObj:(NSManagedObject *)obj
{
    [_context deleteObject:obj];
    NSError *error;
    if (![_context save:&error]) {
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
    }
}

-(void) deleteAll
{
    NSArray *a = [self fetchAll];
    for(EntityAirViolation *easr in a)
        [self deleteObj:easr];
}

@end

//
//  AirFilterSummaryManager.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/10/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirFilterSummaryManager.h"
#import "ConcurMobileAppDelegate.h"
#import "EntityAirFilterSummary.h"

static AirFilterSummaryManager *sharedInstance;

@implementation AirFilterSummaryManager

@synthesize context = _context;
@synthesize entityName;

+(AirFilterSummaryManager*)sharedInstance
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
				sharedInstance = [[AirFilterSummaryManager alloc] init];
			}
		}
		return sharedInstance;
	}
}

// Calling manager should always call the initWithContext Create a new context and send it

-(AirFilterSummaryManager*)init
{
    self = [super init];
	if (self)
	{
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.context = [ad managedObjectContext];
        self.entityName = @"EntityAirFilterSummary";
	}
    
	return self;
}

/**
 Init the manager with a given context
 @param incontext - private context if initiated from a xml parser
 @return new instance of object
 */

-(instancetype)initWithContext:(NSManagedObjectContext*)inContext
{
    self = [super init];
	if (self)
	{
        self.context = inContext;
        self.entityName = @"EntityAirFilterSummary";
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
    
    //    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(code = %@)", code];
    //    [fetchRequest setPredicate:pred];
    
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
    
    //    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"name" ascending:YES];
    //    [fetchRequest setSortDescriptors:[NSArray arrayWithObject:sort]];
    //    [sort release];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
    
}


-(NSManagedObject *) makeNew
{
    return [NSEntityDescription insertNewObjectForEntityForName:entityName inManagedObjectContext:self.context];
}

-(EntityAirViolation*) makeNewViolation
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityAirViolation" inManagedObjectContext:self.context];
}

-(NSManagedObject *) fetchOrMake:(NSString *)key
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
        return [self makeNew];
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
    for(EntityAirFilterSummary *easr in a)
        [self deleteObj:easr];
}


-(EntityAirViolation*) fetchHighestEnforcement:(EntityAirFilterSummary*) air
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirViolation" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    // AirViolation does not honor relAirViolations/relAirFilterSummary.  
    // Instead it stores the fareId of the corresponding airFilterSummary.  
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@)", air.fareId];
    [fetchRequest setPredicate:pred];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"enforcementLevel" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return nil;
}

@end

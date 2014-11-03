//
//  AirFilterManager.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/8/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "AirFilterManager.h"
#import "ConcurMobileAppDelegate.h"
#import "EntityAirShopResults.h"

static AirFilterManager *sharedInstance;

@implementation AirFilterManager
@synthesize context = _context;
@synthesize entityName;

+(AirFilterManager*)sharedInstance
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
				sharedInstance = [[AirFilterManager alloc] init];
			}
		}
		return sharedInstance;
	}
}

// Calling manager should always call the initWithContext Create a new context and send it

-(AirFilterManager*)init
{
    self =[super init];
	if (self)
	{
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.context = [ad managedObjectContext];
        self.entityName = @"EntityAirFilter";
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
    self =[super init];
	if (self)
	{
        self.context = inContext;
        self.entityName = @"EntityAirFilter";
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


-(NSArray*) fetchByFareId:(NSString *)fareId
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@)", fareId];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}


-(NSManagedObject *) fetchByFareIdSegmentPosFlightPos:(NSString *)fareId segPos:(int)segPos flightPos:(int)flightPos
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@) AND (segmentPos = %d) AND (flightPos = %d)", fareId, segPos, flightPos];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return nil;
}


-(NSArray *) fetchByFareIdSegmentPos:(NSString *)fareId segPos:(int)segPos
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@) AND (segmentPos = %d)", fareId, segPos];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];

    return aFetch;
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
    for(EntityAirShopResults *easr in a)
        [self deleteObj:easr];
}

-(NSArray *) fetchAirFilters: (NSString*)fareId
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityAirFilter" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"segmentPos" ascending:YES];
    NSSortDescriptor *sort2 = [[NSSortDescriptor alloc] initWithKey:@"flightPos" ascending:YES];
    
    [fetchRequest setSortDescriptors:@[sort, sort2]];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@)", fareId];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    

    
    return aFetch;
    
}

@end

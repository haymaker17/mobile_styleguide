//
//  AirShopResultsManager.m
//  ConcurMobile
//
//  Created by Paul Kramer on 8/5/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "AirShopResultsManager.h"
#import "ConcurMobileAppDelegate.h"
#import "EntityAirShopResults.h"

static AirShopResultsManager *sharedInstance;

@implementation AirShopResultsManager
@synthesize context = _context;
@synthesize entityName;

+(AirShopResultsManager*)sharedInstance
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
				sharedInstance = [[AirShopResultsManager alloc] init];
			}
		}
		return sharedInstance;
	}
}


-(AirShopResultsManager*)init
{
    self = [super init];
	if (self)
	{
        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
        self.context = [ad managedObjectContext];
        self.entityName = @"EntityAirShopResults";
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
    NSError *error = nil;
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

@end

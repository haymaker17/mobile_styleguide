//
//  HotelBookingManager.m
//  ConcurMobile
//
//  Created by Paul Kramer on 9/23/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import "HotelBookingManager.h"
#import "ConcurMobileAppDelegate.h"
#import "EntityHotelRoom.h"
#import "EntityHotelFee.h"
#import "EntityHotelDetail.h"
#import "EntityHotelViolation.h"

#define SORT_BY_PREFERRED_VENDORS	0
#define SORT_BY_VENDOR_NAMES		1
#define SORT_BY_PRICE				2
#define SORT_BY_DISTANCE			3
#define SORT_BY_RATING				4

static HotelBookingManager *sharedInstance;

@implementation HotelBookingManager
@synthesize context = _context;
@synthesize entityName;

+(HotelBookingManager*)sharedInstance
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
				sharedInstance = [[HotelBookingManager alloc] init];
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
        self.entityName = @"EntityHotelBooking";
	}
    
	return self;
}



#pragma mark -
#pragma mark Expense Types default coredata
-(void) saveIt:(EntityHotelBooking *) obj
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

-(EntityHotelBooking *) fetchFirst
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


-(NSArray *) fetchedAllSorted:(int)sortOrder
{
    NSSortDescriptor *sort =  nil; //[[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES];
    NSSortDescriptor *sort2 = nil; //[[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES];
    
    if (sortOrder == SORT_BY_PREFERRED_VENDORS)
	{
		sort = [[NSSortDescriptor alloc] initWithKey:@"hotelPrefRank" ascending:NO];
        sort2 = [[NSSortDescriptor alloc] initWithKey:@"relCheapRoom.rate" ascending:YES];
	}
	if (sortOrder == SORT_BY_VENDOR_NAMES)
	{
        sort = [[NSSortDescriptor alloc] initWithKey:@"chainName" ascending:YES];
        sort2 = [[NSSortDescriptor alloc] initWithKey:@"relCheapRoom.rate" ascending:YES];
	} 
	else if (sortOrder == SORT_BY_PRICE)
	{
        sort = [[NSSortDescriptor alloc] initWithKey:@"relCheapRoom.rate" ascending:YES];
        sort2 = [[NSSortDescriptor alloc] initWithKey:@"chainName" ascending:YES];
	}
	else if (sortOrder == SORT_BY_DISTANCE)
	{
        sort = [[NSSortDescriptor alloc] initWithKey:@"distance" ascending:YES];
        sort2 = [[NSSortDescriptor alloc] initWithKey:@"hotel" ascending:YES];
	}
	else if (sortOrder == SORT_BY_RATING)
	{
        sort = [[NSSortDescriptor alloc] initWithKey:@"starRating" ascending:NO];
        sort2 = [[NSSortDescriptor alloc] initWithKey:@"relCheapRoom.rate" ascending:YES];
	}

    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    [fetchRequest setSortDescriptors:[NSArray arrayWithObjects:sort, sort2,nil]];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}

-(EntityHotelBooking *) makeNew
{
    return [NSEntityDescription insertNewObjectForEntityForName:entityName inManagedObjectContext:self.context];
}

-(EntityHotelFee *) makeNewFee
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityHotelFee" inManagedObjectContext:self.context];
}

-(EntityHotelRoom *) makeNewRoom
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityHotelRoom" inManagedObjectContext:self.context];
}

-(EntityHotelDetail *) makeNewDetail
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityHotelDetail" inManagedObjectContext:self.context];
}

-(EntityHotelViolation*) makeNewViolation
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityHotelViolation" inManagedObjectContext:self.context];
}

-(EntityHotelBooking *) fetchOrMake:(NSString *)key
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

// Return the booking entity for a given propertyId
-(EntityHotelBooking *) fetchByPropertyId:(NSString *)propertyId
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(propertyId = %@)", propertyId];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
  
    return [aFetch lastObject];
}


-(void) deleteObj:(EntityHotelBooking *)obj
{
    [_context deleteObject:obj];
    NSError *error;
    if (![_context save:&error]) {
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
    }
}

-(void) deleteHotelRoom:(EntityHotelRoom *)obj
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
    for(EntityHotelBooking *booking in a)
        [self deleteObj:booking];
}

-(void) deletePartialResults
{
    NSArray *a = [self fetchAll];
    for(EntityHotelBooking *booking in a)
    {
        if ([booking.isFinal boolValue] == NO)
        {
            [self deleteObj:booking];
        }
    }
}

- (BOOL)isAnyHotelRecommended
{
    NSFetchRequest *fetchRequest = [NSFetchRequest fetchRequestWithEntityName:entityName];
    fetchRequest.predicate = [NSPredicate predicateWithFormat:@"recommendationSource != nil"];
    NSArray *results = [self.context executeFetchRequest:fetchRequest error:0];
    return results.count > 0;
}

-(void) deleteRooms:(EntityHotelBooking *)hotelBooking
{
    for(EntityHotelRoom *room in hotelBooking.relHotelRoom)
        [self deleteHotelRoom:room];
}

-(EntityHotelViolation*) fetchHighestEnforcement:(EntityHotelRoom*) room
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityHotelViolation" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(relHotelRoom = %@)", room];
    [fetchRequest setPredicate:pred];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"enforcementLevel" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    __autoreleasing EntityHotelViolation*result = nil;
    if(aFetch != nil && [aFetch count] > 0)
        result = aFetch[0];
    
    return result;
}

-(NSArray *) fetchViolationsByRoom:(EntityHotelRoom*) room
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityHotelViolation" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(relHotelRoom = %@)", room];
    [fetchRequest setPredicate:pred];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"enforcementLevel" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];
    
    NSError *error;

    __autoreleasing NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if (error != nil)
    {
        [Flurry logError:[NSString stringWithFormat:@"%d", [error code]] message:@"Hotel: fetchViolationsByRoom error" error:error];
    }
    
    return aFetch;
}


-(EntityHotelBooking *) fetchMostSevre:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"exceptionLevel" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    __autoreleasing EntityHotelBooking* result = nil;
    if(aFetch != nil && [aFetch count] > 0)
        result = aFetch[0];
 
    return result;
}

-(NSArray *) fetchByFareId:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(fareId = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"exceptionLevel" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];
    
    NSError *error;
    __autoreleasing NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
}


-(EntityHotelViolation *) fetchViolationByCode:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityHotelViolation" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(code = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    __autoreleasing EntityHotelViolation* result = aFetch[0];
    return result;
}

@end

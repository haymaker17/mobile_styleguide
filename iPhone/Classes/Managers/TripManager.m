//
//  TripManager.m
//  ConcurMobile
//
//  Created by Paul Kramer on 6/11/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "TripManager.h"

static TripManager *sharedInstance;

@implementation TripManager

@synthesize entityName;

+(TripManager*)sharedInstance
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
				sharedInstance = [[TripManager alloc] init];
			}
		}
		return sharedInstance;
	}
}


-(TripManager*)init
{
	if (self = [super init]) 
	{
//        ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
//        self.context = [ad managedObjectContext];
        self.entityName = @"EntityTrip";
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
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"tripEndDateLocal" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
    
}

+(NSArray *) fetchAllWithContext:(NSManagedObjectContext*) manContext
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityTrip" inManagedObjectContext: manContext];
    [fetchRequest setEntity:entity];
    
    NSSortDescriptor *sort = [[NSSortDescriptor alloc] initWithKey:@"tripEndDateLocal" ascending:NO];
    [fetchRequest setSortDescriptors:@[sort]];
    
    NSError *error;
    NSArray *aFetch = [manContext executeFetchRequest:fetchRequest error:&error];
    
    return aFetch;
    
}

+(void) saveItWithContext:(NSManagedObject *) obj manContext:(NSManagedObjectContext*)manContext
{
    NSError *error;
    if (![manContext save:&error])
        NSLog(@"Whoops, couldn't save object: %@", [error localizedDescription]); 
}

+(EntityTrip *) makeNew: (NSManagedObjectContext*)manContext
{
    return [NSEntityDescription insertNewObjectForEntityForName:@"EntityTrip" inManagedObjectContext:manContext];
}

+(EntityBooking *) makeNewBooking:(EntityTrip*)trip manContext:(NSManagedObjectContext*)manContext
{
    EntityBooking *obj = [NSEntityDescription insertNewObjectForEntityForName:@"EntityBooking" inManagedObjectContext:manContext];
    obj.relTrip = trip;
    return obj;
}

+(EntitySegment *) makeNewSegment:(EntityTrip*)trip manContext:(NSManagedObjectContext*)manContext
{
    EntitySegment *obj = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySegment" inManagedObjectContext:manContext];
    obj.relTrip = trip;
    EntitySegmentLocation *startLoc = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySegmentLocation" inManagedObjectContext:manContext];
    obj.relStartLocation = startLoc;
    EntitySegmentLocation *endLoc = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySegmentLocation" inManagedObjectContext:manContext];
    obj.relEndLocation = endLoc;
    
    return obj;
}

+(EntityFlightStats *) makeNewFlightStat:(EntitySegment*)segment  manContext:(NSManagedObjectContext*)manContext
{
    EntityFlightStats *obj = [NSEntityDescription insertNewObjectForEntityForName:@"EntityFlightStats" inManagedObjectContext:manContext];
    obj.relSegment = segment;
    return obj;
}

+(EntityViolation *) makeNewViolation:(EntityTrip*)trip  manContext:(NSManagedObjectContext*)manContext
{
    EntityViolation *obj = [NSEntityDescription insertNewObjectForEntityForName:@"EntityViolation" inManagedObjectContext:manContext];
    obj.relTrip = trip;
    return obj;
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
        return [self makeNew:self.entityName];
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

-(EntityTrip*) fetchByTripKey:(NSString *)key
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(tripKey = %@)", key];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch[0];
    else
        return nil;

}

-(EntityTrip *) fetchByItinLocator:(NSString *)itinLocator
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(itinLocator = %@)", itinLocator];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    return [aFetch lastObject];
}

-(EntityTrip*) fetchTripByBookingRecordLocator:(NSString *)recLoc
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityBooking" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];

    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(recordLocator = %@)", recLoc];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return ((EntityBooking *)aFetch[0]).relTrip;
    else
        return nil;
    
    
}
//Added for MOB-10671
-(EntityTrip*) fetchTripByClientLocator:(NSString *)clientLoc
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntityTrip" inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(clientLocator = %@)", clientLoc];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(aFetch != nil && [aFetch count] > 0)
        return ((EntityTrip *)aFetch[0]);
    else
        return nil;
    
    
}

-(EntitySegment*) fetchBySegmentKey:(NSString*) segmentKey inTrip:(EntityTrip*) trip
{
    for (EntitySegment *seg in trip.relSegment)
    {
        if ([seg.idKey isEqualToString:segmentKey])
            return seg;
    }
    return nil;
}

- (NSArray*)fetchSegmentsByType:(NSString*)segType
{
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(type = %@)", segType];
    return [self fetch:@"EntitySegment" withCondition: pred withContext:self.context];
}


- (EntitySegment*)fetchSegmentByIdKey:(NSString*)idKey tripKey:(NSString*) tripKey
{
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(idKey = %@ and relTrip.tripKey = %@)", idKey, tripKey];
    NSArray* aFetch = [self fetch:@"EntitySegment" withCondition: pred withContext:self.context];

    if(aFetch != nil && [aFetch count] > 0)
        return ((EntitySegment *)aFetch[0]);
    else
        return nil;
}



-(void) deleteObj:(NSManagedObject *)obj
{
    [_context deleteObject:obj];
    NSError *error;
    if (![_context save:&error]) {
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
    }
}

+(void) deleteObjWithContext:(NSManagedObject *)obj manContext:(NSManagedObjectContext*)manContext
{
    [manContext deleteObject:obj];
    NSError *error;
    if (![manContext save:&error]) {
        NSLog(@"Whoops, couldn't delete object: %@", [error localizedDescription]);
    }
}

-(void) deleteAll
{
    NSArray *a = [self fetchAll];
    for(EntityTrip *obj in a)
        [self deleteObj:obj];
}

+(void) deleteAllWithContext:(NSManagedObjectContext*) manContext
{
    NSArray *a = [self fetchAllWithContext:manContext];
    for(EntityTrip *obj in a)
        [self deleteObjWithContext:obj manContext:manContext];
}


-(void) resetContext
{
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    self.context = [ad managedObjectContext];
}

@end

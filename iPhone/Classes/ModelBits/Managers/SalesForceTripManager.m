//
//  SalesForceTripManager.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/18/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "SalesForceTripManager.h"

@implementation SalesForceTripManager

+(EntitySalesForceTrip*) lookupTripBySalesforceId:(NSString*)salesforceId inContext:(NSManagedObjectContext*)context
{
    if (salesforceId == nil || salesforceId.length == 0)
        return nil;
    
	// Find the specified trip.
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySalesForceTrip" inManagedObjectContext:context];
    
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(identifier = %@)", salesforceId];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *tripsEntities = [context executeFetchRequest:fetchRequest error:&error];
    
    
    EntitySalesForceTrip *tripEntity = nil;
    if(tripsEntities != nil && [tripsEntities count] > 0)
	{
        tripEntity = [tripsEntities objectAtIndex:0];
	}
    
    return tripEntity;
}

+(EntitySalesForceTrip*) lookupTripByLocator:(NSString*)locator inContext:(NSManagedObjectContext*)context
{
    if (locator == nil || locator.length == 0)
        return nil;
    
	// Find the specified trip.
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySalesForceTrip" inManagedObjectContext:context];
    
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(locator = %@)", locator];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *tripsEntities = [context executeFetchRequest:fetchRequest error:&error];
    
    
    EntitySalesForceTrip *tripEntity = nil;
    if(tripsEntities != nil && [tripsEntities count] > 0)
	{
        tripEntity = [tripsEntities objectAtIndex:0];
	}
    
    return tripEntity;}


@end

//
//  OfferManager.m
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/2/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//  Helper methods to handle coredata related methods for EntityOffer
//  Also has methods to chekc offer validity. 
//

#import "OfferManager.h"
#import "ImageUtil.h"
#import "TripManager.h"


@implementation OfferManager

static OfferManager *sharedInstance;
@synthesize entityName;
@synthesize hasValidOffers;

+(OfferManager*)sharedInstance
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
				sharedInstance = [[OfferManager alloc] init];
			}
		}
		return sharedInstance;
	}
}

+(BOOL) hasValidProximity:(EntityOffer *)offer
{
    //If there are no location constraints then offer should not be invalidated
    if ([offer.relOfferLocation count] == 0 )
    {
        return TRUE;
    }
    else
    {
        for (EntityOfferLocation *validLocation in offer.relOfferLocation)
        {
            CLLocation *location = [[CLLocation alloc] initWithLatitude:[validLocation.latitude doubleValue] longitude:[validLocation.longitude doubleValue]];
            double distance = [location distanceFromLocation:[GlobalLocationManager currentLocation]];
            if(distance/1000 <= [validLocation.proximity doubleValue])
            {
                return TRUE;
            }
        }
    }
    
    return FALSE;
    
}

+(BOOL) hasValidTimeRange:(EntityOffer *)offer
{
    
    if ([offer.relOfferTimeRange count] == 0 )
    {
        return TRUE;
    }
    else
    {
       
       NSDate *currentDate = [DateTimeFormatter getCurrentLocalDateTimeInGMT];
        for (EntityOfferTimeRange *validTimeRange in offer.relOfferTimeRange)
        {
             if ([validTimeRange.startDateTimeUTC timeIntervalSinceDate:currentDate] <= 0 && [validTimeRange.endDateTimeUTC timeIntervalSinceDate:currentDate] > 0) {
                return YES;
            }
        }
    }

    return FALSE;

}

-(OfferManager*)init
{
    self = [super init];
	if (self)
	{
        self.entityName = @"EntityOffer";
	}
    
	return self;
}


-(EntityOffer *) makeNew:(NSManagedObjectContext*) manContext
{
    return ((EntityOffer *)[NSEntityDescription insertNewObjectForEntityForName:@"EntityOffer" inManagedObjectContext:manContext]);
}

-(EntityOfferLocation *) makeNewOfferLocation:(EntityOffer*)offer 
{
    EntityOfferLocation *obj = ((EntityOfferLocation *)[NSEntityDescription insertNewObjectForEntityForName:@"EntityOfferLocation" inManagedObjectContext:offer.managedObjectContext]);
    obj.relOffer = offer;
    return obj;
}

-(EntityOfferTimeRange *) makeNewOfferTimeRange:(EntityOffer*)offer 
{
    EntityOfferTimeRange *obj = ((EntityOfferTimeRange *)[NSEntityDescription insertNewObjectForEntityForName:@"EntityOfferTimeRange" inManagedObjectContext:offer.managedObjectContext]);
    obj.relOffer = offer;
    return obj;
}

-(EntityOfferOverlay *) makeNewOfferOverlay:(EntityOffer *)offer
{
    EntityOfferOverlay *obj = ((EntityOfferOverlay *)[NSEntityDescription insertNewObjectForEntityForName:@"EntityOfferOverlay" inManagedObjectContext:offer.managedObjectContext]);
    obj.relOffer = offer;
    return obj;
}


-(void) processImageDataWithBlock:(void (^)(NSData *imageData))processImage offer:(EntityOffer*)offer
{
    
    NSString *url = nil;

    if (offer.imageURL != nil) {
        url = offer.imageURL;
    }
    else
    {
        NSString * name = [NSString stringWithFormat:@"%@@2x.png",offer.imageName];

        url = [NSString stringWithFormat:@"%@/images/mobile/intouch/iOS/%@", [ExSystem sharedInstance].entitySettings.uriNonSSL,name];
    }

	dispatch_queue_t downloadQueue = dispatch_queue_create("Offer icon downloader", NULL);
	dispatch_async(downloadQueue, ^{
		NSData *imageData = [ImageUtil imageDataForImageWithURLString:url];
		dispatch_async(dispatch_get_main_queue(), ^{
		    processImage(imageData);
		});
	});
}


//Get offer with given.
-(NSArray*) fetchAllOffersWithContext:(NSManagedObjectContext *)manContext
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: manContext];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [manContext executeFetchRequest:fetchRequest error:&error];
        
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch ;
    else
        return nil;
    
}


//Get offer data by segment idkey.
-(NSArray*) fetchOffersBySegIdKey:(NSString *)segIdKey
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(segmentKey = %@)", segIdKey];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(error!=nil)
    {
        NSLog(@"Error while fetching offers : %@",error);
    }
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch ;
    else
        return nil;
    
}

-(NSArray*) fetchOffersBySegIdKeyAndSegmentSide:(NSString *)segIdKey segmentSide:(NSString *)segmentSide
{
    NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:entityName inManagedObjectContext: self.context];
    [fetchRequest setEntity:entity];
    
    NSPredicate *pred = [NSPredicate predicateWithFormat:@"(segmentKey = %@) and (segmentSide = %@)", segIdKey, segmentSide];
    [fetchRequest setPredicate:pred];
    
    NSError *error;
    NSArray *aFetch = [self.context executeFetchRequest:fetchRequest error:&error];
    
    if(error!=nil)
    {
        NSLog(@"Error while fetching offers : %@",error);
    }
    if(aFetch != nil && [aFetch count] > 0)
        return aFetch ;
    else
        return nil;
    
}

-(void) saveItWithContext:(NSManagedObjectContext*)manContext
{
    NSError *error;
    if (![manContext save:&error])
        NSLog(@"Whoops, couldn't save object: %@", [error localizedDescription]);
}


-(void) deleteObjWithContext:(NSManagedObject *)obj 
{
    NSError *error;
    NSManagedObjectContext *context = obj.managedObjectContext;
    [context deleteObject:obj];

    if (![context save:&error]) {
        NSLog(@"Whoops, couldn't delete EntityOffer object: %@", [error localizedDescription]);
    }
}

-(void) deleteAllOffers:(NSManagedObjectContext*) manContext
{
    NSArray *a = [self fetchAllOffersWithContext:manContext];
    // Delete all the offers
    for(EntityOffer *offer in a)
    {
        [self deleteObjWithContext:offer];
    }
}


-(void) deleteOffersWithoutSegmentData:(NSManagedObjectContext*) manContext
{
    NSArray *offers = [self fetchAllOffersWithContext:manContext];
    NSArray *trips = [TripManager fetchAllWithContext:manContext];
    // Delete all the offers that do not have any corresponding trip/segments deleted
    for(EntityOffer *offer in offers)
    {
        for(EntityTrip *trip in trips)
        {
            NSString *segKey = offer.segmentKey;
            EntitySegment *seg = [[TripManager sharedInstance] fetchBySegmentKey:segKey inTrip:trip];
            if(seg == nil)
            {
                [self deleteObjWithContext:offer];
            }
        }
    }
}


@end

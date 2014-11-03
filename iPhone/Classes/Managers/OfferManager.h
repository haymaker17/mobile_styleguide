//
//  OfferManager.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/2/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "BaseManager.h"
#import "EntityOffer.h"
#import "DateTimeFormatter.h"
#import "EntityOfferLocation.h"
#import "EntityOfferTimeRange.h"
#include "EntityOfferOverlay.h"

@interface OfferManager : BaseManager {
    NSString *entityName;
    BOOL hasValidOffers;
}

@property (nonatomic, strong) NSString *entityName;
@property BOOL hasValidOffers;

+(OfferManager*)sharedInstance;

-(EntityOffer *) makeNew:(NSManagedObjectContext*) manContext;
-(EntityOfferLocation *) makeNewOfferLocation:(EntityOffer*)offer ;
-(EntityOfferTimeRange *) makeNewOfferTimeRange:(EntityOffer*)offer ;
-(EntityOfferOverlay *) makeNewOfferOverlay:(EntityOffer*)offer;
-(void) saveItWithContext:(NSManagedObjectContext*)manContext;
-(void) deleteAllOffers:(NSManagedObjectContext*) manContext;
-(void) deleteOffersWithoutSegmentData:(NSManagedObjectContext*)manContext;
-(void) deleteObjWithContext:(NSManagedObject *)obj ;

-(NSArray*) fetchOffersBySegIdKey:(NSString *)segIdKey;
-(NSArray*) fetchOffersBySegIdKeyAndSegmentSide:(NSString *)segIdKey segmentSide:(NSString *)segmentSide;

-(void) processImageDataWithBlock:(void (^)(NSData *imageData))processImage offer:(EntityOffer *)offer;
+(BOOL) hasValidProximity:(EntityOffer *)offer ;
+(BOOL) hasValidTimeRange:(EntityOffer *)offer ;


@end

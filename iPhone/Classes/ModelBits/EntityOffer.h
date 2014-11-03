//
//  EntityOffer.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityOfferLocation, EntityOfferOverlay, EntityOfferTimeRange;

@interface EntityOffer : NSManagedObject

@property (nonatomic, retain) NSString * offerAction;
@property (nonatomic, retain) NSString * segmentSide;
@property (nonatomic, retain) NSString * segmentKey;
@property (nonatomic, retain) NSString * actionURL;
@property (nonatomic, retain) NSString * recordLocator;
@property (nonatomic, retain) NSString * title;
@property (nonatomic, retain) NSString * imageURL;
@property (nonatomic, retain) NSString * offerId;
@property (nonatomic, retain) NSString * offerVendor;
@property (nonatomic, retain) NSString * imageName;
@property (nonatomic, retain) NSString * htmlContent;
@property (nonatomic, retain) NSString * offerType;
@property (nonatomic, retain) NSString * bookingSource;
@property (nonatomic, retain) NSNumber * geoLatitude;
@property (nonatomic, retain) NSNumber * geoLongitude;
@property (nonatomic, retain) NSString * offerApplication;
@property (nonatomic, retain) NSNumber * geoDimensionkm;
@property (nonatomic, retain) NSSet *relOfferLocation;
@property (nonatomic, retain) NSSet *relOfferTimeRange;
@property (nonatomic, retain) NSSet *relOverlay;
@end

@interface EntityOffer (CoreDataGeneratedAccessors)

- (void)addRelOfferLocationObject:(EntityOfferLocation *)value;
- (void)removeRelOfferLocationObject:(EntityOfferLocation *)value;
- (void)addRelOfferLocation:(NSSet *)values;
- (void)removeRelOfferLocation:(NSSet *)values;

- (void)addRelOfferTimeRangeObject:(EntityOfferTimeRange *)value;
- (void)removeRelOfferTimeRangeObject:(EntityOfferTimeRange *)value;
- (void)addRelOfferTimeRange:(NSSet *)values;
- (void)removeRelOfferTimeRange:(NSSet *)values;

- (void)addRelOverlayObject:(EntityOfferOverlay *)value;
- (void)removeRelOverlayObject:(EntityOfferOverlay *)value;
- (void)addRelOverlay:(NSSet *)values;
- (void)removeRelOverlay:(NSSet *)values;

@end

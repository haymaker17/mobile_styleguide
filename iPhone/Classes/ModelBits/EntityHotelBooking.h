//
//  EntityHotelBooking.h
//  ConcurMobile
//
//  Created by Shifan Wu on 6/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityHotelCheapRoom, EntityHotelDetail, EntityHotelFee, EntityHotelImage, EntityHotelRoom;

@interface EntityHotelBooking : NSManagedObject

@property (nonatomic, retain) NSString * chainCode;
@property (nonatomic, retain) NSString * phone;
@property (nonatomic, retain) NSString * propertyId;
@property (nonatomic, retain) NSNumber * starRating;
@property (nonatomic, retain) NSString * country;
@property (nonatomic, retain) NSString * state;
@property (nonatomic, retain) NSString * propertyUri;
@property (nonatomic, retain) NSNumber * travelPoints;
@property (nonatomic, retain) NSNumber * fusion14RecommendationScore;
@property (nonatomic, retain) NSNumber * recommendationScore;
@property (nonatomic, retain) NSNumber * isAddtional;
@property (nonatomic, retain) NSNumber * isFusion14Recommended;
@property (nonatomic, retain) NSString * tollFree;
@property (nonatomic, retain) NSNumber * companyPriority;
@property (nonatomic, retain) NSString * choiceId;
@property (nonatomic, retain) NSString * addr1;
@property (nonatomic, retain) NSString * distanceUnit;
@property (nonatomic, retain) NSNumber * isSoldOut;
@property (nonatomic, retain) NSNumber * benchmarkPrice;
@property (nonatomic, retain) NSNumber * hotelPrefRank;
@property (nonatomic, retain) NSNumber * distance;
@property (nonatomic, retain) NSNumber * isContract;
@property (nonatomic, retain) NSNumber * isCompanyPreferredChain;
@property (nonatomic, retain) NSString * zip;
@property (nonatomic, retain) NSString * countryCode;
@property (nonatomic, retain) NSNumber * lng;
@property (nonatomic, retain) NSString * city;
@property (nonatomic, retain) NSString * chainName;
@property (nonatomic, retain) NSString * recommendationDisplayValue;
@property (nonatomic, retain) NSNumber * isFinal;
@property (nonatomic, retain) NSNumber * lat;
@property (nonatomic, retain) NSNumber * isNoRates;
@property (nonatomic, retain) NSString * addr2;
@property (nonatomic, retain) NSString * gdsName;
@property (nonatomic, retain) NSNumber * contractRate;
@property (nonatomic, retain) NSString * stateAbbrev;
@property (nonatomic, retain) NSString * hotel;
@property (nonatomic, retain) NSNumber * cheapestRoomRate;
@property (nonatomic, retain) NSString * recommendationSource;
@property (nonatomic, retain) NSString * preferenceType;
@property (nonatomic, retain) NSString * benchmarkCurrency;
@property (nonatomic, retain) NSNumber * isFedRoom;
@property (nonatomic, retain) EntityHotelCheapRoom *relCheapRoom;
@property (nonatomic, retain) NSSet *relHotelFee;
@property (nonatomic, retain) EntityHotelCheapRoom *relCheapRoomViolation;
@property (nonatomic, retain) NSSet *relHotelDetail;
@property (nonatomic, retain) NSSet *relHotelRoom;
@property (nonatomic, retain) NSSet *relHotelImage;
@end

@interface EntityHotelBooking (CoreDataGeneratedAccessors)

- (void)addRelHotelFeeObject:(EntityHotelFee *)value;
- (void)removeRelHotelFeeObject:(EntityHotelFee *)value;
- (void)addRelHotelFee:(NSSet *)values;
- (void)removeRelHotelFee:(NSSet *)values;

- (void)addRelHotelDetailObject:(EntityHotelDetail *)value;
- (void)removeRelHotelDetailObject:(EntityHotelDetail *)value;
- (void)addRelHotelDetail:(NSSet *)values;
- (void)removeRelHotelDetail:(NSSet *)values;

- (void)addRelHotelRoomObject:(EntityHotelRoom *)value;
- (void)removeRelHotelRoomObject:(EntityHotelRoom *)value;
- (void)addRelHotelRoom:(NSSet *)values;
- (void)removeRelHotelRoom:(NSSet *)values;

- (void)addRelHotelImageObject:(EntityHotelImage *)value;
- (void)removeRelHotelImageObject:(EntityHotelImage *)value;
- (void)addRelHotelImage:(NSSet *)values;
- (void)removeRelHotelImage:(NSSet *)values;

@end

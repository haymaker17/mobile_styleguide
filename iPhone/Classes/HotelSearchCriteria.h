//
//  HotelSearchCriteria.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "LocationResult.h"
#import "EntityHotel.h"

@interface HotelSearchCriteria : NSObject
{
}

@property (strong, nonatomic) NSDate				*checkinDate;
@property (strong, nonatomic) NSDate				*checkoutDate;
@property (strong, nonatomic) LocationResult		*locationResult;
@property (strong, nonatomic) NSNumber				*distanceValue;
@property (strong, nonatomic) NSNumber				*isMetricDistance;
@property (strong, nonatomic) NSString				*containingWords;
@property (nonatomic) NSUInteger					smokingIndex;
@property (strong, nonatomic) NSDecimalNumber       *perDiemRate;

@property (strong, nonatomic) NSArray				*smokingPreferenceCodes;
@property (strong, nonatomic) NSArray				*smokingPreferenceNames;

-(id)init;
-(void)setNextDayCheckout;

#pragma mark -
#pragma mark Last Entity
-(EntityHotel *) loadEntity;
-(void) saveEntity;
-(void) clearEntity:(EntityHotel *) ent;
-(void)writeToSettings;
-(void)readFromSettings;

@end

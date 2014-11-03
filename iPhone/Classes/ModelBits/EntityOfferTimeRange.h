//
//  EntityOfferTimeRange.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityOffer;

@interface EntityOfferTimeRange : NSManagedObject

@property (nonatomic, retain) NSDate * startDateTimeUTC;
@property (nonatomic, retain) NSDate * endDateTimeUTC;
@property (nonatomic, retain) EntityOffer *relOffer;

@end

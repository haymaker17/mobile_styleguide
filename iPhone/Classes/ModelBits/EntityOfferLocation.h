//
//  EntityOfferLocation.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityOffer;

@interface EntityOfferLocation : NSManagedObject

@property (nonatomic, retain) NSNumber * dimension;
@property (nonatomic, retain) NSNumber * latitude;
@property (nonatomic, retain) NSNumber * proximity;
@property (nonatomic, retain) NSNumber * longitude;
@property (nonatomic, retain) EntityOffer *relOffer;

@end

//
//  EntityOfferOverlay.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 11/6/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityOffer;

@interface EntityOfferOverlay : NSManagedObject

@property (nonatomic, retain) NSString * overLay;
@property (nonatomic, retain) EntityOffer *relOffer;

@end

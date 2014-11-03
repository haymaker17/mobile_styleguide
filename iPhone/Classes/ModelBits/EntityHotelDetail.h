//
//  EntityHotelDetail.h
//  ConcurMobile
//
//  Created by Paul Kramer on 9/28/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityHotelBooking;

@interface EntityHotelDetail : NSManagedObject

@property (nonatomic, strong) NSString * name;
@property (nonatomic, strong) NSString * descript;
@property (nonatomic, strong) EntityHotelBooking *relHotelBooking;

@end

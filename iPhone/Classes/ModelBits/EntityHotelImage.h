//
//  EntityHotelImage.h
//  ConcurMobile
//
//  Created by Paul Kramer on 9/23/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityHotelBooking;

@interface EntityHotelImage : NSManagedObject

@property (nonatomic, strong) NSString * imageURI;
@property (nonatomic, strong) NSString * thumbURI;
@property (nonatomic, strong) EntityHotelBooking *relHotelBooking;

@end

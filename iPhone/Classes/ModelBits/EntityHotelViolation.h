//
//  EntityHotelViolation.h
//  ConcurMobile
//
//  Created by ernest cho on 8/27/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>

@class EntityHotelCheapRoom, EntityHotelRoom;

@interface EntityHotelViolation : NSManagedObject

@property (nonatomic, strong) NSString * code;
@property (nonatomic, strong) NSNumber * enforcementLevel;
@property (nonatomic, strong) NSString * message;
@property (nonatomic, strong) NSString * violationType;
@property (nonatomic, strong) EntityHotelCheapRoom *relCheapRoom;
@property (nonatomic, strong) EntityHotelRoom *relHotelViolationCurrent;
@property (nonatomic, strong) EntityHotelRoom *relHotelRoom;

@end

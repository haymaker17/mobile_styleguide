//
//  EntityAirCriteria.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 1/5/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityAirCriteria : NSManagedObject

@property (nonatomic, strong) NSDate * DepartureDate;
@property (nonatomic, strong) NSString * ArrivalCity;
@property (nonatomic, strong) NSString * DepartureAirportCode;
@property (nonatomic, strong) NSString * ReturnAirportCode;
@property (nonatomic, strong) NSString * ClassOfService;
@property (nonatomic, strong) NSString * DepartureCity;
@property (nonatomic, strong) NSDate * ReturnDate;
@property (nonatomic, strong) NSNumber * ReturnTime;
@property (nonatomic, strong) NSNumber * DepartureTime;
@property (nonatomic, strong) NSNumber * refundableOnly;

@end

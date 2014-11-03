//
//  HotelBenchmark.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 06/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface HotelBenchmark : NSObject

@property (nonatomic, strong) NSString *currency;
@property (nonatomic) double distanceAmount;
@property (nonatomic, strong) NSString *distanceUnits;
@property (nonatomic, strong) NSString *location;
@property (nonatomic, strong) NSString *name;
@property (nonatomic) double price;
@property (nonatomic, strong) NSString *subdivCode;

@end

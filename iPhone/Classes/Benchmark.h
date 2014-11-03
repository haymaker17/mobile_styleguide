//
//  Benchmark.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 21/11/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface Benchmark : NSObject

@property (strong, nonatomic) NSString *crnCode;
@property (strong, nonatomic) NSDate *date;
@property (strong, nonatomic) NSString *destination;
@property (strong, nonatomic) NSString *origin;
@property (strong, nonatomic) NSNumber *price;
@property BOOL roundtrip;
@end


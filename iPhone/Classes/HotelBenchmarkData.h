//
//  HotelBenchmarkData.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 06/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MsgResponder.h"

@interface HotelBenchmarkData : MsgResponder

@property (nonatomic) BOOL isSuccess;
@property (nonatomic, strong) NSString *message;
@property (nonatomic, strong) NSArray *benchmarksList;

+(NSArray *)getHotelBenchmarksFromXml:(NSString *)xml atPath:(NSString *)path;
+(NSString *)getBenchmarkRangeFromBenchmarks:(NSArray *)benchmarks;

@end

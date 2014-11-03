//
//  AirBenchmarkData.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 06/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MsgResponder.h"
#import "Benchmark.h"

@interface AirBenchmarkData : MsgResponder

@property (nonatomic) BOOL isSuccess;
@property (nonatomic, strong) Benchmark *benchmark;
@property (nonatomic, strong) NSString *message;

@end

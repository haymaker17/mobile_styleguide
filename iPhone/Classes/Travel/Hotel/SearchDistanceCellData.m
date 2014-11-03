//
//  SearchDistanceCellData.m
//  ConcurMobile
//
//  Created by Sally Yan on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "SearchDistanceCellData.h"

@implementation SearchDistanceCellData

-(instancetype)init
{
    self = [super init];
    
    if (self) {
        self.searchDistanceOptions = [self getDistanceVaulesAndUnit];
    }
    return self;
}

- (NSArray *)getDistanceValues
{
    NSArray *distanceValues = @[@"1", @"2", @"5", @"10", @"15", @"25", @"Greater than 25"];
    return distanceValues;
}

- (NSString *)getDistanceUnit
{
    NSLocale *locale = [NSLocale currentLocale];
    BOOL isMetric = [[locale objectForKey:NSLocaleUsesMetricSystem] boolValue];
    if (isMetric) {
        return @"KM";
    }
    return @"Mile(s)";
}

- (NSDictionary *)getDistanceVaulesAndUnit
{
    NSDictionary *dictionary = @{@"distanceValues": [self getDistanceValues],
                                 @"distanceUnit" : [self getDistanceUnit]};
    return dictionary;
}

@end

//
//  JsonParser.m
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "JsonParser.h"

@implementation JsonParser

+ (NSString *)getNodeAsString:(NSString *)nodeName json:(NSDictionary *)json
{
    NSString *string = [json objectForKey:nodeName];
    if (!string) {
        string = @"";
    } else if ([string isKindOfClass:[NSNull class]]) {
        string = @"";
    }
    return string;
}

+ (double)getNodeAsDouble:(NSString *)nodeName json:(NSDictionary *)json
{
    NSString *valueAsString = [self getNodeAsString:nodeName json:json];
    return [valueAsString doubleValue];
}

+ (float)getNodeAsFloat:(NSString *)nodeName json:(NSDictionary *)json
{
    NSString *valueAsString = [self getNodeAsString:nodeName json:json];
    return [valueAsString floatValue];
}

+ (int)getNodeAsInt:(NSString *)nodeName json:(NSDictionary *)json
{
    NSString *valueAsString = [self getNodeAsString:nodeName json:json];
    return [valueAsString intValue];
}

// TODO: verify this is sufficient
+ (BOOL)getNodeAsBOOL:(NSString *)nodeName json:(NSDictionary *)json
{
    NSString *tmp = [self getNodeAsString:nodeName json:json];
    if ([@"true" isEqualToString:tmp]) {
        return YES;
    } else {
        return NO;
    }
}

@end
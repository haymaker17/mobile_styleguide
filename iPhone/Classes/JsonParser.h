//
//  JsonParser.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 17/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

@interface JsonParser : NSObject

+ (NSString *)getNodeAsString:(NSString *)nodeName json:(NSDictionary *)json;
+ (double)getNodeAsDouble:(NSString *)nodeName json:(NSDictionary *)json;
+ (float)getNodeAsFloat:(NSString *)nodeName json:(NSDictionary *)json;
+ (int)getNodeAsInt:(NSString *)nodeName json:(NSDictionary *)json;
+ (BOOL)getNodeAsBOOL:(NSString *)nodeName json:(NSDictionary *)json;

@end
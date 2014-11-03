//
//  InstrumentationPrototype.h
//  InstrumentationPrototype
//
//  Created by Matt Cannizzaro on 4/9/13.
//  Copyright (c) 2013 AppDynamics Technologies. All rights reserved.
//

#import <Foundation/Foundation.h>
/** AppDynamics EUM Instrumentation SDK
 */
@interface ADEumInstrumentation : NSObject

/** Initialize Instrumentation
 @param appKey AppDynamics EUM Application Key
 */
+ (void)initWithKey:(NSString *)appKey;

+ (void)initWithKey:(NSString *)appKey enableLogging:(BOOL)enableLogging;

+ (void)initWithKey:(NSString *)appKey collectorUrl:(NSString*)collectorUrl enableLogging:(bool)enableLogging;

/** Change the application key. Older metrics/reports will be discarded when app key is changed.
 */
+ (void)changeAppKey:(NSString*)appKey;
@end

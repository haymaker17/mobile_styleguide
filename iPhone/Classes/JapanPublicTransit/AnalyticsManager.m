//
//  AnalyticsManager.m
//  ConcurMobile
//
//  Created by Richard Puckett on 11/6/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AnalyticsManager.h"

@implementation AnalyticsManager

__strong static id _sharedInstance = nil;

+ (AnalyticsManager *)sharedInstance {
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _sharedInstance = [[self alloc] init];
    });
    
    return _sharedInstance;
}

- (id)init {
    self = [super init];
    
    if (self) {
        self.impressionStack = [[CXStack alloc] init];
        self.impressionMap = [[NSMutableDictionary alloc] init];
    }
    
    return self;
}

- (void)clearImpressionPath {
    [self.impressionMap removeAllObjects];
    [self.impressionStack clear];
}

- (NSString *)ancestorFromNames:(NSArray *)names {
    NSString *ancestor = nil;
    
    for (NSString *name in names) {
        ancestor = [self.impressionMap valueForKey:name];
        
        if (ancestor != nil) {
            break;
        }
    }
    
    return ancestor;
}

- (void)logCategory:(NSString *)category
           withName:(NSString *)name {
    
    NSString *flurryEvent = [NSString stringWithFormat:@"%@: %@", category, name];

    [Flurry logEvent:flurryEvent];
}

- (void)logCategory:(NSString *)category
           withName:(NSString *)name
            andType:(NSString *)type {
    
    NSDictionary *dictionary = @{@"Type": type};
        
    NSString *flurryEvent = [NSString stringWithFormat:@"%@: %@", category, name];
    
    //NSLog(@"Flurry Log: %@ %@", flurryEvent, dictionary);
    
    [Flurry logEvent:flurryEvent withParameters:dictionary];
}

- (void)logCategory:(NSString *)category
           withName:(NSString *)name
      fromAncestors:(NSArray *)names {
    
    NSString *ancestorName = [self ancestorFromNames:names];
    
    // Don't send garbage.
    //
    if (ancestorName != nil) {
        NSDictionary *dictionary = @{@"From": ancestorName};
        
        NSString *flurryEvent = [NSString stringWithFormat:@"%@: %@", category, name];
        
        //NSLog(@"Flurry Log: %@ %@", flurryEvent, dictionary);
        
        [Flurry logEvent:flurryEvent withParameters:dictionary];
    }
}

- (void)logCategory:(NSString *)category
           withName:(NSString *)name
               from:(NSString *)from {
    
    NSDictionary *dictionary = @{@"From": from};
    
    NSString *flurryEvent = [NSString stringWithFormat:@"%@: %@", category, name];
    
    //NSLog(@"Flurry Log: %@ %@", flurryEvent, dictionary);
    
    [Flurry logEvent:flurryEvent withParameters:dictionary];
}

- (void)popImpression:(NSString *)name {
    [self.impressionStack pop];
    [self.impressionMap removeObjectForKey:name];
}

- (void)pushImpression:(NSString *)name {
    [self.impressionStack push:name];
    [self.impressionMap setValue:name forKey:name];
}

@end

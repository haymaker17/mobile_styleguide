//
//  RouteManager.m
//  ConcurMobile
//
//  Created by Richard Puckett on 9/19/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "RouteManager.h"

@implementation RouteManager

__strong static id _sharedInstance = nil;

+ (RouteManager *)sharedInstance {
    static dispatch_once_t once;
    
    dispatch_once(&once, ^{
        _sharedInstance = [[self alloc] init];
    });
    
    return _sharedInstance;
}

- (id)init {
    self = [super init];
    
    if (self) {
        self.recentSearchRoutes = [self loadRecentSearchRoutes];
    }
    
    return self;
}

- (NSArray *)fetchRecentSearchRoutes {
    return self.recentSearchRoutes;
}

- (BOOL)isRouteSaved:(Route *)route {
    BOOL isSaved = NO;
    
    for (Route *r in self.recentSearchRoutes) {
        if ([r.uuid isEqualToString:route.uuid]) {
            isSaved = YES;
            break;
        }
    }
    
    return isSaved;
}

- (NSMutableArray *)loadRecentSearchRoutes {
    NSMutableArray *searchRoutes = [[NSMutableArray alloc] init];
    
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSArray *serializedSearchRoutes = [defaults arrayForKey:@"recent_search_routes"];
    
    if ([serializedSearchRoutes count] > 0) {
        for (NSData *routeData in serializedSearchRoutes) {
            Route *route = [NSKeyedUnarchiver unarchiveObjectWithData:routeData];
            [searchRoutes addObject:route];
        }
    }
    
    return searchRoutes;
}

- (void)saveRecentSearchRoutes:(NSArray *)routes {
    NSUserDefaults *defaults = [NSUserDefaults standardUserDefaults];
    
    NSMutableArray *mutableRoutes = [[NSMutableArray alloc] init];
    
    for (Route *route in routes) {
        NSData *data = [NSKeyedArchiver archivedDataWithRootObject:route];
        
        [mutableRoutes addObject:data];
    }
    
    [defaults setObject:mutableRoutes forKey:@"recent_search_routes"];
    
    [defaults synchronize];
}

- (void)saveRecentSearchRoute:(Route *)route withMaxHistory:(NSUInteger)maxHistory {
    
    // Only save route if it's not already saved.
    //
    if (![self isRouteSaved:route]) {
        Route *routeCopy = [route copy];
        
        [self.recentSearchRoutes insertObject:routeCopy atIndex:0];
        
        int numSearches = [self.recentSearchRoutes count];
        int numTooMany = numSearches - maxHistory;
        
        for (int i = 0; i < numTooMany; i++) {
            [self.recentSearchRoutes removeLastObject];
        }
        
        [self saveRecentSearchRoutes:self.recentSearchRoutes];
    }
}

@end

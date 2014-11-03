//
//  RouteManager.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/19/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "Route.h"

@interface RouteManager : NSObject

@property (strong, nonatomic) NSMutableArray *recentSearchRoutes;

+ (RouteManager *)sharedInstance;

- (NSArray *)fetchRecentSearchRoutes;

- (BOOL)isRouteSaved:(Route *)route;
- (void)saveRecentSearchRoute:(Route *)route withMaxHistory:(NSUInteger)maxHistory;

@end

//
//  AbstractRouteSource.h
//  ConcurMobile
//
//  Created by Richard Puckett on 9/4/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "Route.h"
#import "RouteExpense.h"

@protocol RouteSourceDelegate;

@interface AbstractRouteSource : NSObject <UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) id<RouteSourceDelegate> delegate;

- (NSString *)emptyMessage;

@end

@protocol RouteSourceDelegate <NSObject>

- (void)didDeleteLastItemForRouteSource:(AbstractRouteSource *)routeSource;

- (void)routeSource:(AbstractRouteSource *)routeSource
     didSelectRoute:(Route *)route;

@end

@protocol SavedRouteSourceDelegate <NSObject>

- (void)routeSource:(AbstractRouteSource *)routeSource
    didSelectSavedExpense:(RouteExpense *)re;

@end
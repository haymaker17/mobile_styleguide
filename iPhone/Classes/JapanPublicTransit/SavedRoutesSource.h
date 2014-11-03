//
//  SavedRoutesSource.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AbstractRouteSource.h"
#import "RouteExpense.h"

@protocol SavedRouteSourceDelegate;

@interface SavedRoutesSource : AbstractRouteSource

@property (weak, nonatomic) id<SavedRouteSourceDelegate> savedRouteSourceDelegate;

@end

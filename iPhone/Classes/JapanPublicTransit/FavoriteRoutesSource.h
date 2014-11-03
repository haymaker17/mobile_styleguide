//
//  FavoriteRoutesSource.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/23/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>

#import "AbstractRouteSource.h"

@interface FavoriteRoutesSource : AbstractRouteSource

@property (weak, nonatomic) id<SavedRouteSourceDelegate> favoriteRouteSourceDelegate;

@end

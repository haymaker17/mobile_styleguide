//
//  OfficeLocationsViewController.h
//  ConcurMobile
//
//  Created by Christopher Butcher on 08/08/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AbstractDataSourceDelegate.h"
#import "CTELocation.h"

@interface OfficeLocationsViewController : UITableViewController <AbstractDataSourceDelegate, UISearchDisplayDelegate>

@property (nonatomic,copy) void(^onLocationSelected)(CTELocation *cteLocation);
@property (nonatomic,strong) CTELocation *currentLocation;

@end

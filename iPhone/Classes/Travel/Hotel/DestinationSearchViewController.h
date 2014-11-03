//
//  DestinationSearchViewController.h
//  ConcurMobile
//
//  Created by Pavan Adavi on 7/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AbstractDataSourceDelegate.h"
#import "CTELocation.h"

@interface DestinationSearchViewController : UITableViewController <AbstractDataSourceDelegate, UISearchDisplayDelegate,UISearchBarDelegate>

@property (nonatomic,copy) void(^onLocationSelected)(CTELocation *cteLocation);
@property (nonatomic,strong) CTELocation *selectedLocation;
@end

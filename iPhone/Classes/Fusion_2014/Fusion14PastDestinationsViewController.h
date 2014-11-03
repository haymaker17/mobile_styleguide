//
//  Fusion14PastDestinationsViewController.h
//  ConcurMobile
//
//  Created by Sally Yan on 3/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@interface Fusion14PastDestinationsViewController : UITableViewController

@property (assign) EvaSearchCategory category;

+ (void) showPastHotelDestinations:(UINavigationController*)navi;
+ (void) showPastFlightDestinations:(UINavigationController*)navi;

@end

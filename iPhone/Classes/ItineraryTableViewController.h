//
//  ItineraryTableViewController.h
//  ConcurMobile
//
//  Created by Wes Barton on 3/27/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ItineraryConfig;

@interface ItineraryTableViewController : UITableViewController<UIActionSheetDelegate>

@property NSMutableDictionary *paramBag;

@property (strong, nonatomic) NSString	*role;

@property (weak, nonatomic) IBOutlet UINavigationItem *navBar;

@property ItineraryConfig *itineraryConfig;
@property (strong, nonatomic) IBOutlet UIBarButtonItem *AllowanceBarButton;

@end

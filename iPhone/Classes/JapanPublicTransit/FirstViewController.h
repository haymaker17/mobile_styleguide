//
//  FirstViewController.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/12/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

#import "AbstractRouteSource.h"
#import "FavoriteRoutesSource.h"
#import "RecentRoutesSource.h"
#import "SavedRoutesSource.h"

extern NSInteger const TYPE_RECENT;
extern NSInteger const TYPE_FAVORTIES;
extern NSInteger const TYPE_SAVED;

extern NSInteger const ADD_ROUTE_SEARCH;
extern NSInteger const ADD_ROUTE_MANUAL;

@interface FirstViewController : UIViewController <RouteSourceDelegate, SavedRouteSourceDelegate, UIActionSheetDelegate>

@property (weak, nonatomic) IBOutlet UIButton *addNewRouteButton;
@property (weak, nonatomic) IBOutlet UIView *emptyView;
@property (weak, nonatomic) IBOutlet UILabel *emptyViewMessage;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UIView *typeControlBackground;
@property (weak, nonatomic) IBOutlet UISegmentedControl *typeControl;

@property (strong, nonatomic) UIBarButtonItem *doneButton;
@property (strong, nonatomic) UIBarButtonItem *editButton;

@property (weak, nonatomic) AbstractRouteSource *currentRoutesSource;
@property (strong, nonatomic) FavoriteRoutesSource *favoriteRoutesSource;
@property (strong, nonatomic) RecentRoutesSource *recentRoutesSource;
@property (strong, nonatomic) SavedRoutesSource *savedRoutesSource;

- (IBAction)addNewRouteButtonTapped:(id)sender;
- (IBAction)typeChanged:(id)sender;

- (void)rightNavButtonTapped:(id)sender;

@end

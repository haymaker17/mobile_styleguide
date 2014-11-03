//
//  AddRouteSearchViewController.h
//  JapanPublicTransit
//
//  Created by Richard Puckett on 8/22/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "AddRouteSearchViewController.h"
#import "DCRoundSwitch.h"
#import "Route.h"
#import "RouteSearchModel.h"

extern NSInteger const ROUTE_ATTRIBUTE_DATE;
extern NSInteger const ROUTE_ATTRIBUTE_DEPARTURE_STATION;
extern NSInteger const ROUTE_ATTRIBUTE_ARRIVAL_STATION;
extern NSInteger const ROUTE_ATTRIBUTE_THROUGH_STATION_1;
extern NSInteger const ROUTE_ATTRIBUTE_THROUGH_STATION_2;
extern NSInteger const ROUTE_ATTRIBUTE_SEAT_TYPE;

@interface AddRouteSearchViewController : UIViewController <UITableViewDataSource, UITableViewDelegate>

@property (strong, nonatomic) IBOutlet UITableView *tableView;

@property (strong, nonatomic) UIBarButtonItem *doneButton;
@property (strong, nonatomic) NSString *rightButtonText;

@property (strong, nonatomic) DCRoundSwitch *roundTripToggle;

@property (strong, nonatomic) RouteSearchModel *routeSearchModel;

@property (assign) BOOL usingThroughStations;

@property (strong, nonatomic) DCRoundSwitch *icCardFareToggle;

@end

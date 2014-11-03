//
//  Fusion14FlightSearchResultsViewController.h
//  ConcurMobile
//
//  Created by Sally Yan on 4/23/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AirShop.h"
#import "ExMsgRespondDelegate.h"
#import "AirFilterManager.h"
#import "AirFilter.h"
#import "EntityAirFilter.h"
#import "EntityAirShopResults.h"
#import "EntityAirCriteria.h"

@interface Fusion14FlightSearchResultsViewController : UITableViewController <ExMsgRespondDelegate, NSFetchedResultsControllerDelegate>

@property (nonatomic, strong) AirShop *airShop;
@property (nonatomic, strong) NSMutableDictionary   *vendors;
@property BOOL shouldGetAllResults;

@end

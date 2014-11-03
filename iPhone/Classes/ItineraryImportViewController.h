//
//  ItineraryImportViewController.h
//  ConcurMobile
//
//  Created by Wes Barton on 5/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ItineraryConfig;
@class Itinerary;

@interface ItineraryImportViewController : UITableViewController

@property NSMutableDictionary *paramBag;
@property (strong, nonatomic) NSString	*role;
@property ItineraryConfig *itineraryConfig;
@property Itinerary *currentItinerary;

@property NSMutableArray *imports;

@property (nonatomic, copy) void(^onSuccessfulImport)(NSDictionary *);
@property (nonatomic, copy) void(^onFailedImport)(NSMutableArray *);

@end

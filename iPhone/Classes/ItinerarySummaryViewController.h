//
//  ItinerarySummaryViewController.h
//  ConcurMobile
//
//  Created by Wes Barton on 6/18/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class ItineraryConfig;
@class Itinerary;

@interface ItinerarySummaryViewController : UITableViewController

@property Itinerary *itinerary;
@property ItineraryConfig *itineraryConfig;

@property (nonatomic, copy) void(^onSuccessfulSave)(NSDictionary *);



@end

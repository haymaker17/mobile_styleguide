//
//  ItineraryStopViewController.h
//  ConcurMobile
//
//  Created by Wes Barton on 1/16/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class Itinerary;
@class ItineraryConfig;


static const int HeaderSection = 0;
static const int InformationHeader = 1;
static const int StopSection = 2;

@interface ItineraryStopViewController : UITableViewController

@property NSMutableDictionary *paramBag;

@property NSString *selectedItinKey;

@property (strong, nonatomic) UIView *waitView;
@property (strong, nonatomic) UIActivityIndicatorView *activityIndicator;
@property (strong, nonatomic) UILabel *waitLabel;

@property (weak, nonatomic) IBOutlet UINavigationItem *navBar;
@property (weak, nonatomic)IBOutlet UIBarButtonItem *saveGenerateButton;

@property (strong, nonatomic) NSString	*role;

@property(nonatomic) BOOL hasItineraries;
@property ItineraryConfig *itineraryConfig;
@property Itinerary *itinerary;

@property (nonatomic, copy) void(^onSuccessfulSaveOfSingleDay)(NSDictionary *);

@end



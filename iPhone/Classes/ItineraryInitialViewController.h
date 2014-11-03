//
//  ItineraryInitialViewController.h
//  ConcurMobile
//
//  Created by Wes Barton on 5/7/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
@class ItineraryConfig;
@class Itinerary;

@interface ItineraryInitialViewController : UITableViewController

@property NSMutableDictionary *paramBag;
@property (strong, nonatomic) NSString	*role;
@property (strong, nonatomic) IBOutlet UINavigationItem *navBar;
@property BOOL hasCloseButton;

@property (strong, nonatomic) IBOutlet UIBarButtonItem *AddItineraryButton;
@property(nonatomic, strong) NSMutableArray *itineraries;
@property(nonatomic, strong) ItineraryConfig *itineraryConfig;

- (void)actionBack:(id)sender;
@end

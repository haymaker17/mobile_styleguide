//
//  Fusion14HotelSearchResultsViewController.h
//  ConcurMobile
//
//  Created by Sally Yan on 3/25/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "HotelSearch.h"
#import "HotelSearchCriteria.h"
#import "HotelSearchResultsViewController.h"
#import "FindHotels.h"
#import "ExMsgRespondDelegate.h"
#import "EntityHotelBooking.h"
#import "EntityHotelCheapRoom.h"
#import "EntityHotelImage.h"
#import "HotelBookingManager.h"


@interface Fusion14HotelSearchResultsViewController : UITableViewController < ExMsgRespondDelegate, NSFetchedResultsControllerDelegate >

@property(strong, nonatomic) HotelSearch *hotelSearch;
@property(strong, nonatomic) HotelSearchResultsViewController *searchResultsVC;
@property (nonatomic, strong) RotatingRoundedRectView           *pollingView;


@end

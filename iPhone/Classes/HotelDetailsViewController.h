//
//  HotelDetailsViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"
#import "HotelSummaryDelegate.h"
#import "EntityHotelBooking.h"
#import "EntityHotelDetail.h"
#import "RoomListSummaryCell.h"

@class HotelSearch;
@class HotelSearchCriteria;

@interface HotelDetailsViewController : BookingBaseTableViewController <UITableViewDelegate, UITableViewDataSource, HotelSummaryDelegate>
{
	HotelSearch	*hotelSearch;
	NSArray		*tableSections, *aFees;
    EntityHotelBooking *hotelBooking;
}

@property (nonatomic, strong) HotelSearch	*hotelSearch;
@property (nonatomic, strong) NSArray		*tableSections;
@property (nonatomic, strong) NSArray		*aFees;
@property (nonatomic, strong) EntityHotelBooking *hotelBooking;

- (void)makeToolbar;
- (void)makeTableSections;
-(void)configureCell:(RoomListSummaryCell*)cell hotel:(EntityHotelBooking *)hotelResult showAddressLink:(BOOL)showAddressLink;
@end

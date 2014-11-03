//
//  HotelCreditCardViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 7/8/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"
#import "TrainDetailVC.h"
#import "AirShopFilteredResultsVC.h"

@class UserConfig;


@interface HotelCreditCardViewController : BookingBaseTableViewController <UITableViewDelegate, UITableViewDataSource>
{
	NSNumber					*creditCardIndex;
	NSString					*fromView;
	TrainDetailVC				*parentVC;
    BOOL                        isFromAir;
    AirShopFilteredResultsVC    *airShopFilteredResultsVC;
    NSArray                     *creditCards;
}

@property (nonatomic, strong) AirShopFilteredResultsVC    *airShopFilteredResultsVC;
@property BOOL isFromAir;
@property (nonatomic, strong) NSNumber						*creditCardIndex;
@property (nonatomic, strong) NSString						*fromView;
@property (nonatomic, strong) TrainDetailVC					*parentVC;
@property (nonatomic, strong) NSArray                       *creditCards;

-(void)closeView;


@end

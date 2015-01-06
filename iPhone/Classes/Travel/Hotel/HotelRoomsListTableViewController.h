//
//  HotelRoomsListTableViewController.h
//  ConcurMobile
//
//  Created by Sally Yan on 8/4/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "AbstractDataSourceDelegate.h"
#import "HotelRoomsListDataSource.h"
#import "MobileAlertView.h"
#import "HotelSearchCriteriaV2.h"

@interface HotelRoomsListTableViewController : UITableViewController <AbstractDataSourceDelegate, UICollectionViewDataSource, UICollectionViewDelegate>

@property (nonatomic, strong) CTEHotelCellData *hotelCellData;
@property (nonatomic, strong) HotelSearchCriteriaV2 *searchCriteria;

@end

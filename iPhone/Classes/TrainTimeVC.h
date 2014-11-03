//
//  TrainTimeVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "BookingCellData.h"

@interface TrainTimeVC : MobileViewController <UITableViewDelegate, UITableViewDataSource> {
	NSMutableArray		*aTimes;
	UITableView			*tableList;
	BookingCellData		*bcdDate, *bcdTime;
	UILabel				*lblDepartureDate;
	BOOL				isReturn;
}

@property (strong, nonatomic) NSMutableArray		*aTimes;
@property (strong, nonatomic) IBOutlet 	UITableView			*tableList;

@property (strong, nonatomic) BookingCellData		*bcdTime;
@property (strong, nonatomic) BookingCellData		*bcdDate;
@property (strong, nonatomic) IBOutlet 	UILabel		*lblDepartureDate;

@property BOOL				isReturn;

-(void) initTimeList;
@end

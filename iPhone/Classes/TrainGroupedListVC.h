//
//  TrainGroupedListVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 12/7/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"


@interface TrainGroupedListVC : MobileViewController <UITableViewDelegate, UITableViewDataSource>{
	
	UILabel		*lblFromLabel, *lblFrom, *lblToLabel, *lblTo, *lblDateRange;
	UITableView	*tableList;
	NSMutableArray	*aKeys;
	NSMutableDictionary *dictGroups;
	UIView			*vNothing;
	UILabel			*lblNothing;
}

@property (strong, nonatomic) IBOutlet UILabel		*lblFromLabel;
@property (strong, nonatomic) IBOutlet UILabel		*lblFrom;
@property (strong, nonatomic) IBOutlet UILabel		*lblToLabel;
@property (strong, nonatomic) IBOutlet UILabel		*lblTo;
@property (strong, nonatomic) IBOutlet UILabel		*lblDateRange;
@property (strong, nonatomic) IBOutlet UITableView	*tableList;

@property (strong, nonatomic) NSMutableArray		*aKeys;
@property (strong, nonatomic) NSMutableDictionary	*dictGroups;

@property (strong, nonatomic) IBOutlet UIView			*vNothing;
@property (strong, nonatomic) IBOutlet UILabel			*lblNothing;

@property (strong, nonatomic) NSMutableArray        *taFields;  // TravelAuth fields for GOV

@end

//
//  TripViolationSummaryVC.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 17/07/2013.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "EntityTrip.h"
#import "TripToApprove.h"

@interface TripViolationSummaryVC : MobileViewController <UITableViewDataSource,UITableViewDelegate>

@property (strong,nonatomic) IBOutlet UITableView *tableView;

@property (nonatomic, strong) EntityTrip *trip;
//@property (nonatomic, strong) TripToApprove *tripToApprove;

@property (strong, nonatomic) NSString *lblNameText;
@property (strong, nonatomic) NSString *lblDateText;
@property (strong, nonatomic) NSString *lblAmountText;
@property (strong, nonatomic) NSString *lblBottomText;

@end

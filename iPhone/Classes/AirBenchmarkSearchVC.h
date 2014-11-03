//
//  AirBenchmarkSearchVC.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 08/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "LocationDelegate.h"

@interface AirBenchmarkSearchVC : MobileViewController <UITableViewDataSource, UITableViewDelegate, LocationDelegate, DateEditDelegate, DateTimePopoverDelegate>

@property (weak, nonatomic) IBOutlet UISegmentedControl *segmentTripDirection;
@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UILabel *lblHeader;
@property (strong, nonatomic) IBOutlet UIView *viewContainingTripDirectionSegment;
- (IBAction)changedOneWayRoundTripValue:(UISegmentedControl *)sender;

- (instancetype)initWithTitle:(NSString*)title;
@end

//
//  HotelBenchmarkSearchVC.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 16/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "LocationDelegate.h"
#import "PickerDelegate.h"

@interface HotelBenchmarkSearchVC : MobileViewController <UITableViewDataSource, UITableViewDelegate, LocationDelegate, PickerDelegate>

@property (strong, nonatomic) IBOutlet UILabel *lblHeader;
@property (strong, nonatomic) IBOutlet UITableView *tableView;

- (instancetype)initWithTitle:(NSString*)title;

@end

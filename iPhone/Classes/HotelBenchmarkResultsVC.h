//
//  HotelBenchmarkResultsVC.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 20/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileViewController.h"

@interface HotelBenchmarkResultsVC : MobileViewController <UITableViewDataSource, UITableViewDelegate>

@property (weak, nonatomic) IBOutlet UITableView *tableView;
@property (weak, nonatomic) IBOutlet UILabel *lblHeader;

@property (strong, nonatomic) NSArray *benchmarksList;// of HotelBenchmark objects
@property (strong, nonatomic) NSString *searchLocation;
@property (strong, nonatomic) NSString *monthOfStayString;
@property (strong, nonatomic) NSString *distanceString;
@property (strong, nonatomic) NSString *headerText;

- (instancetype)initWithTitle:(NSString*)title;

@end

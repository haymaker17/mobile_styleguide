//
//  AirBenchmarkResultsVC.h
//  ConcurMobile
//
//  Created by Deepanshu Jain on 13/01/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "MobileViewController.h"
#import "Benchmark.h"

@interface AirBenchmarkResultsVC : MobileViewController <UITableViewDataSource, UITableViewDelegate>

@property (nonatomic, strong) Benchmark *benchmarkData;
@property (nonatomic, strong) NSString *fromAirportFullName;
@property (nonatomic, strong) NSString *toAirportFullName;

@property (weak, nonatomic) IBOutlet UILabel *lblHeader;
@property (strong, nonatomic) IBOutlet UITableView *tableView;

@end

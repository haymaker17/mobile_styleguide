//
//  HotelViolationReasonTableViewController.m
//  ConcurMobile
//
//  Created by ernest cho on 10/9/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "HotelViolationReasonTableViewController.h"
#import "ViolationReasonTableViewCell.h"

// using legacy ConcurMobile code here
#import "SystemConfig.h"
#import "ViolationReason.h"

typedef void(^ViolationReasonSelectionBlock)(NSString *selectedReasonCode, NSString *selectedReasonDescription);

@interface HotelViolationReasonTableViewController ()

@property (nonatomic, readonly, strong) NSArray *violationReasons;
@property (nonatomic, readonly, weak) NSString *selectedReasonCode;

// callback block for when a violation reason is selected
@property (nonatomic, readonly, copy) ViolationReasonSelectionBlock violationReasonSelectionBlock;

@end

@implementation HotelViolationReasonTableViewController

- (id)initWithReason:(NSString *)selectedReasonCode completion:(void (^)(NSString *selectedReasonCode, NSString *selectedReasonDescription))completion
{
    self = [super init];
    if (self) {
        _selectedReasonCode = selectedReasonCode;
        _violationReasonSelectionBlock = completion;

        [self prepareViolationReasonsFromSystemConfig];

        // I register a nib instead of a class.  I like it better that way.
        [self.tableView registerNib:[UINib nibWithNibName:@"ViolationReasonTableViewCell" bundle:nil] forCellReuseIdentifier:@"violationReasonTableViewCell"];
    }
    return self;
}

- (void)prepareViolationReasonsFromSystemConfig
{
    NSMutableArray *tmp = [[NSMutableArray alloc] init];

    // easier to work with this as a list rather than a dictionary
    [[SystemConfig getSingleton].hotelViolationReasons enumerateKeysAndObjectsUsingBlock:^(id key, id obj, BOOL *stop) {
        [tmp addObject:obj];
    }];

    _violationReasons = tmp;
}

- (void)viewDidLoad {
    [super viewDidLoad];
}

- (void)didReceiveMemoryWarning {
    [super didReceiveMemoryWarning];
}

#pragma mark - Table view data source

- (NSInteger)numberOfSectionsInTableView:(UITableView *)tableView {
    return 1;
}

- (NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section {
    return self.violationReasons.count;
}

- (UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath {
    ViolationReasonTableViewCell *cell = (ViolationReasonTableViewCell *)[self.tableView dequeueReusableCellWithIdentifier:@"violationReasonTableViewCell" forIndexPath:indexPath];
    ViolationReason *tmp = [self.violationReasons objectAtIndex:indexPath.row];

    // ViolationReason should not have the description overriden like this, but it's old code and I dont feel safe changing it
    [cell.violationReason setText:tmp.description];

    if ([tmp.code isEqual:self.selectedReasonCode]) {
        cell.accessoryType = UITableViewCellAccessoryCheckmark;
    } else {
        cell.accessoryType = UITableViewCellAccessoryNone;
    }

    return cell;
}

-(CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath
{
    return 70.0;
}

#pragma mark - Table view delegate

// In a xib-based application, navigation from a table can be handled in -tableView:didSelectRowAtIndexPath:
- (void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath
{
    if (self.violationReasonSelectionBlock) {
        ViolationReason *tmp = self.violationReasons[indexPath.row];
        self.violationReasonSelectionBlock(tmp.code, tmp.description);
    }

    [self.navigationController popViewControllerAnimated:YES];
}

@end

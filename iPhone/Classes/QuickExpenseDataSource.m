//
//  QuickExpenseDataSource.m
//  ConcurMobile
//
//  Created by Shifan Wu on 11/1/13.
//  Copyright (c) 2013 Concur. All rights reserved.
//

#import "QuickExpenseDataSource.h"
#import "QEFormVC.h"

@interface QuickExpenseDataSource ()

@property (nonatomic,strong) QEFormVC *oldQEFormVC;

@end


@implementation QuickExpenseDataSource


- (id) init{
    if (self = [super init]) {
        self.oldQEFormVC = [[QEFormVC alloc] init];
        [self.oldQEFormVC viewDidLoad];
    }
    return self;
}


-(NSInteger)numberOfFields{
    NSInteger total = 0;
    
    NSInteger oldSections = [self.oldQEFormVC numberOfSectionsInTableView:self.formTableView];
    for (int i =0 ; i<oldSections; i++) {
        total += [self.oldQEFormVC  tableView:self.formTableView numberOfRowsInSection:i];
    }
    return total;
}

- (UITableViewCell*)tableView:(UITableView *)tableView fieldForIndex:(NSInteger)index{
    
    NSInteger total = 0;
    NSInteger totalMinusOne = 0;
    
    NSInteger oldSections = [self.oldQEFormVC numberOfSectionsInTableView:self.formTableView];
    
    for (int section =0 ; section<oldSections; section++) {
        total += [self.oldQEFormVC  tableView:self.formTableView numberOfRowsInSection:section];
        if (index < total) {
            return [self.oldQEFormVC tableView:self.formTableView cellForRowAtIndexPath:[NSIndexPath indexPathForRow:(index-totalMinusOne) inSection:section]];
        }
        totalMinusOne = total;
    }
    assert(@"we should never hit this point");
    return nil;
    
}


@end

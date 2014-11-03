//
//  IgniteMeetingDetailVC.h
//  ConcurMobile
//
//  Created by Yiwen Wu on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "EntitySalesOpportunity.h"
#import "EntitySegment.h"
#import "IgnitePopoverModalDelegate.h"
#import "IgniteSegmentEditDelegate.h"

@interface IgniteMeetingDetailVC : MobileViewController <
    UITableViewDelegate, 
    UITableViewDataSource>
{
    UITableView                     *tableList;
    UINavigationBar                 *navBar;

    EntitySegment                   *segment;
    EntitySalesOpportunity          *opportunity;
    
    NSMutableArray                  *sections;
    id<IgnitePopoverModalDelegate, IgniteSegmentEditDelegate>  __weak _delegate;
}

@property (nonatomic, strong) IBOutlet UINavigationBar      *navBar;
@property (nonatomic, strong) IBOutlet UITableView          *tableList;
@property (nonatomic, strong) EntitySegment                 *segment;
@property (nonatomic, strong) EntitySalesOpportunity        *opportunity;
@property (nonatomic, strong) NSMutableArray                *sections;
@property (nonatomic, weak) id<IgnitePopoverModalDelegate, IgniteSegmentEditDelegate> delegate;

- (void)setSeedData:(id<IgnitePopoverModalDelegate, IgniteSegmentEditDelegate>)del withSegment:(EntitySegment *)seg;

@end

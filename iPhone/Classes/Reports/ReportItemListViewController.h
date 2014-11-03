//
//  ReportItemListViewController.h
//  ConcurMobile
//
//  Created by yiwen on 5/19/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReportViewControllerBase.h"
#import "ExpenseTypeDelegate.h"
#import "ItemizedToolbarHelper.h"

@interface ReportItemListViewController : ReportViewControllerBase
            <ExpenseTypeDelegate>
{
    EntryData                   *entry;
    ItemizedToolbarHelper       *itemTbHelper;
}

@property (strong, nonatomic) EntryData					*entry;
@property (strong, nonatomic) ItemizedToolbarHelper		*itemTbHelper;

// Init data
- (void)setSeedData:(ReportData*)report entry:(EntryData*)thisEntry role:(NSString*) curRole;
- (void)loadEntry:(EntryData*) thisEntry withReport:(ReportData*) report;
- (void)recalculateSections;
//-(void) initFields;

-(void)buttonAddPressed:(id) sender;
-(void) actionOnNoData:(id)sender;

@end

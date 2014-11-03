//
//  ReportEntryItemViewController.h
//  ConcurMobile
//
//  Created by yiwen on 5/19/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReportEntryViewController.h"
#import "ItemizedToolbarHelper.h"

@interface ReportEntryItemViewController : ReportEntryViewController {
    EntryData                   *parentEntry;
    ItemizedToolbarHelper       *itemTbHelper;
}

@property (strong, nonatomic) EntryData					*parentEntry;
@property (strong, nonatomic) ItemizedToolbarHelper		*itemTbHelper;

// Init data
//- (void)refreshView;
//- (void)recalculateSections;
- (void)setSeedData:(ReportData*)report entry:(EntryData*)thisEntry item:(EntryData*)thisItem role:(NSString*) curRole;
- (void) loadItem:(EntryData*) thisItem withEntry:(EntryData*) thisEntry withReport:(ReportData*) report;


@end

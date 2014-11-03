//
//  ItemizeHotelViewController.h
//  ConcurMobile
//
//  Created by yiwen on 1/13/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ReportViewControllerBase.h"
#import "ItemizedToolbarHelper.h"

@interface ItemizeHotelViewController : ReportViewControllerBase 
{
	EntryData				*entry;
    ItemizedToolbarHelper   *itemTbHelper;
    FormFieldData           *expTypeField;
}

@property (strong, nonatomic) EntryData					*entry;
@property (strong, nonatomic) ItemizedToolbarHelper     *itemTbHelper;

@property (strong, nonatomic) FormFieldData             *expTypeField;

- (void)setSeedData:(ReportData*)report entry:(EntryData*)thisEntry role:(NSString*) curRole;
- (void)loadEntry:(EntryData*) thisEntry withReport:(ReportData*) report;

- (void) initFields;
- (void)recalculateSections;

@end

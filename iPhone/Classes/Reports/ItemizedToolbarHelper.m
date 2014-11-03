//
//  ItemizedToolbarHelper.m
//  ConcurMobile
//
//  Created by yiwen on 5/24/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import "ItemizedToolbarHelper.h"
#import "FormatUtils.h"

@implementation ItemizedToolbarHelper

@synthesize itemizedAmount, remainingAmount;


#pragma mark - Init data
- (void)setSeedData:(EntryData*)thisEntry
{
    [self calculateRemainingAmounts:thisEntry];
}

-(void) calculateRemainingAmounts:(EntryData*)parentEntry
{
	double remainingAmt = [parentEntry.transactionAmount doubleValue];
	double itemizedAmt = 0.0;
	for (int ix = 0; ix < [parentEntry.itemKeys count]; ix++)
	{
		NSString *rpeKey = (parentEntry.itemKeys)[ix];
		EntryData* item = (parentEntry.items)[rpeKey];
		if (item != nil)
		{
			double itemAmt = [item.transactionAmount doubleValue];
			itemizedAmt += itemAmt;
			remainingAmt -= itemAmt;
		}
	}
	self.itemizedAmount = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", itemizedAmt] crnCode:parentEntry.transactionCrnCode];
	self.remainingAmount = [FormatUtils formatMoney:[NSString stringWithFormat:@"%f", remainingAmt] crnCode:parentEntry.transactionCrnCode];
}

-(void)setupToolbar:(MobileViewController*) vc
{
    // ##TODO## Check whether remaining is zero?
    UIView *rView = [[UIView alloc] initWithFrame:CGRectMake(0, 0, 200, 50)];
    
    UILabel *itemizedText = [[UILabel alloc] initWithFrame:CGRectMake(0, 5, 200, 20)];
    UILabel *remainingText = [[UILabel alloc] initWithFrame:CGRectMake(0, 25, 200, 20)];
    
    NSString* itemizedStr = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Itemized:"], self.itemizedAmount];
    NSString* remainingStr = [NSString stringWithFormat:@"%@ %@", [Localizer getLocalizedText:@"Remaining:"], self.remainingAmount];
    [itemizedText setTextColor:[UIColor whiteColor]];
    [itemizedText setText:itemizedStr];
    [itemizedText setFont:[UIFont systemFontOfSize:17.0f]];
    
    [itemizedText setBackgroundColor:[UIColor clearColor]];
    [itemizedText setTextAlignment:NSTextAlignmentCenter];
    [itemizedText setNumberOfLines:1];
    
    [itemizedText setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
    [itemizedText setShadowOffset:CGSizeMake(0.0f, -1.0f)];
    
    [remainingText setTextColor:[UIColor whiteColor]];
    [remainingText setText:remainingStr];
    [remainingText setFont:[UIFont boldSystemFontOfSize:17.0f]];
    [remainingText setBackgroundColor:[UIColor clearColor]];
    [remainingText setTextAlignment:NSTextAlignmentCenter];
    [remainingText setNumberOfLines:1];
    [remainingText setShadowColor:[UIColor colorWithWhite:0.0f alpha:0.5f]];
    [remainingText setShadowOffset:CGSizeMake(0.0f, -1.0f)];
    
    if ([ExSystem is7Plus]) {
        [itemizedText setTextColor:[UIColor blackColor]];
        [itemizedText setShadowColor:[UIColor clearColor]];
        
        [remainingText setTextColor:[UIColor blackColor]];
        [remainingText setShadowColor:[UIColor clearColor]];
    }
    
    [rView addSubview:itemizedText];
    [rView addSubview:remainingText];
    
    UIBarButtonItem *titleItem = [[UIBarButtonItem alloc] initWithCustomView:rView];
    titleItem.tag = 999;
    
    [vc.navigationController.toolbar setHidden:NO];
    
    NSMutableArray *toolbarItems = [NSMutableArray arrayWithCapacity:5];
    UIBarButtonItem *flexibleSpace = [UIBarButtonItem alloc];
    flexibleSpace = [flexibleSpace initWithBarButtonSystemItem:UIBarButtonSystemItemFlexibleSpace target:nil action:nil];
    [toolbarItems addObject:flexibleSpace];
    [toolbarItems addObject:titleItem];
    [toolbarItems addObject:flexibleSpace];
    
    
    [vc setToolbarItems:toolbarItems animated:YES];
}


@end

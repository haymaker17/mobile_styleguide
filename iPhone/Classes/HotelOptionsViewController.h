//
//  HotelOptionsViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/23/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseTableViewController.h"


@interface HotelOptionsViewController : BookingBaseTableViewController <UITableViewDelegate, UITableViewDataSource>
{
	NSString				*fromView;
	NSString				*optionTypeId;
	NSString				*optionTitle;
	NSArray					*labels;
	int						selectedRowIndex;
	CGFloat					preferredFontSize;
}

@property (nonatomic, strong) NSString				*fromView;
@property (nonatomic, strong) NSString				*optionTypeId;
@property (nonatomic, strong) NSString				*optionTitle;
@property (nonatomic, strong) NSArray				*labels;
@property (nonatomic) int							selectedRowIndex;
@property (nonatomic) CGFloat						preferredFontSize;

-(void)updateTitle;
-(void)scrollToSelectedRow;

@end

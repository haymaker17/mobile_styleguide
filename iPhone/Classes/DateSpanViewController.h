//
//  HotelDatesViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 6/22/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "BookingBaseViewController.h"
#import "DateSpanDelegate.h"
#import "SettingsBaseCell.h"

@interface DateSpanViewController : BookingBaseViewController <UITableViewDelegate, UITableViewDataSource>
{
	UINavigationBar			*tBar;
	UIBarButtonItem			*cancelBtn;
	UIBarButtonItem			*doneBtn;
	UIBarButtonItem			*titleButton;
	UIDatePicker			*datePicker;
	UILabel					*startDateLabel;
	UILabel					*endDateLabel;
	UILabel					*startDateValueLabel;
	UILabel					*endDateValueLabel;
	UIButton				*startDateButton;
	UIButton				*endDateButton;
	NSString				*title;
	NSDate					*startDate;
	NSDate					*endDate;
	NSTimeInterval			timeInterval;
	BOOL					isStartDateSelected;
	id<DateSpanDelegate>	dateSpanDelegate;
    
    UITableView             *tableList;
    NSMutableArray          *aValues;
    NSIndexPath             *indexPathSelected;
}

@property (strong, nonatomic) NSIndexPath             *indexPathSelected;
@property (strong, nonatomic) NSMutableArray            *aValues;
@property (strong, nonatomic) IBOutlet UINavigationBar		*tBar;
@property (strong, nonatomic) IBOutlet UIBarButtonItem	*cancelBtn;
@property (strong, nonatomic) IBOutlet UIBarButtonItem	*doneBtn;
@property (strong, nonatomic) IBOutlet UIBarButtonItem	*titleButton;
@property (strong, nonatomic) IBOutlet UIDatePicker		*datePicker;
@property (strong, nonatomic) IBOutlet UILabel			*startDateLabel;
@property (strong, nonatomic) IBOutlet UILabel			*endDateLabel;
@property (strong, nonatomic) IBOutlet UILabel			*startDateValueLabel;
@property (strong, nonatomic) IBOutlet UILabel			*endDateValueLabel;
@property (strong, nonatomic) IBOutlet UIButton			*startDateButton;
@property (strong, nonatomic) IBOutlet UIButton			*endDateButton;
@property (strong, nonatomic) NSString					*title;
@property (strong, nonatomic) NSDate					*startDate;
@property (strong, nonatomic) NSDate					*endDate;
@property (nonatomic)         NSTimeInterval			timeInterval;
@property (nonatomic)		  BOOL						isStartDateSelected;
@property (strong, nonatomic) id<DateSpanDelegate>		dateSpanDelegate;
@property (strong, nonatomic) IBOutlet UITableView             *tableList;

-(IBAction) startDateButtonPressed:(id)sender;
-(IBAction) endDateButtonPressed:(id)sender;

-(IBAction) datePickerValueChanged:(id)sender;
-(IBAction) btnCancel:(id)sender;
-(IBAction) btnDone:(id)sender;

-(void) initStartDate:(NSDate *)startDt endDate:(NSDate*)endDt selectStartDate:(BOOL)selStartDate title:(NSString*)titleText;
-(void)synchronizeDatePicker;
-(void)closeView;

@end

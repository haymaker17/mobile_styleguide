//
//  DateTimeVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 7/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "BookingCellData.h"
#import "RotatingDatePicker.h"
#import "TrainBookVC.h"
#import "ExtendedHour.h"
#import "SettingsBaseCell.h"

@interface DateTimeVC : MobileViewController  <UIScrollViewDelegate, UIPickerViewDelegate, UITableViewDelegate, UITableViewDataSource> {
	RotatingDatePicker		*datePicker;
	UILabel				*lblDate, *lblTime, *lblDepartureDate, *lblDepartureTime;
	NSDate				*initialDate;
	NSInteger			initialExtendedHour;
	BOOL				isReturn, isTime;
	UIBarButtonItem		*bbiTitle;
	UIScrollView		*scroller;
	UIPickerView		*pickerView;
	NSMutableArray		*aTimes;
	NSInteger			selectedExtendedHour;
	UIButton			*btnBack1, *btnBack2;
	int					dateDiff;
	
	UINavigationBar		*tBar;
	UIBarButtonItem		*cancelBtn;
	UIBarButtonItem		*doneBtn;
    
    UITableView             *tableList;
    NSMutableArray          *aValues;
    NSIndexPath             *indexPathSelected;
    BOOL                    isDateSelected;
}
@property BOOL                    isDateSelected;
@property (strong, nonatomic) NSIndexPath             *indexPathSelected;
@property (strong, nonatomic) NSMutableArray            *aValues;
@property (strong, nonatomic) IBOutlet UITableView             *tableList;

@property (strong, nonatomic) IBOutlet RotatingDatePicker		*datePicker;
@property (strong, nonatomic) IBOutlet UILabel			*lblDate;
@property (strong, nonatomic) IBOutlet UILabel			*lblTime;
@property (strong, nonatomic) IBOutlet UILabel			*lblDepartureDate;
@property (strong, nonatomic) IBOutlet UILabel			*lblDepartureTime;
@property (strong, nonatomic) NSDate					*initialDate;
@property (nonatomic) NSInteger							initialExtendedHour;
@property (strong, nonatomic) IBOutlet UIBarButtonItem		*bbiTitle;
@property (strong, nonatomic) IBOutlet UIScrollView		*scroller;
@property (strong, nonatomic) IBOutlet UIPickerView		*pickerView;

@property (strong, nonatomic) IBOutlet UINavigationBar		*tBar;
@property (strong, nonatomic) IBOutlet UIBarButtonItem		*cancelBtn;
@property (strong, nonatomic) IBOutlet UIBarButtonItem		*doneBtn;

@property (strong, nonatomic) NSMutableArray		*aTimes;
@property (nonatomic) NSInteger						selectedExtendedHour;

@property (strong, nonatomic) IBOutlet UIButton			*btnBack1;
@property (strong, nonatomic) IBOutlet UIButton			*btnBack2;

@property BOOL				isReturn;
@property BOOL				isTime;
@property int					dateDiff;

-(IBAction) datePickerValueChanged:(id)sender;

-(IBAction) closeDown:(id)sender;

-(IBAction) closeDone:(id)sender;

-(void) onDone;

-(void) initTimeList;
-(void)setDateTimeViews;

-(IBAction)switchToTime:(id)sender;

-(IBAction)switchToDate:(id)sender;

-(void)setSelectedTime;

-(NSDate *)addDaysToDate:(NSDate *)dateDepart NumDaysToAdd:(int)daysToAdd;

-(NSString*) textFromDate:(NSDate*)date;
-(NSDate*) dateFromText:(NSString*)text;

-(NSString*)getTitleLabel;
-(NSString*)getDepartureDateLabel;
-(NSString*)getDepartureTimeLabel;

@end

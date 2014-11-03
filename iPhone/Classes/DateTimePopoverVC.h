//
//  DateTimePopoverVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 10/11/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "ExtendedHour.h"
#import "DateTimePopoverDelegate.h"

@interface DateTimePopoverVC : MobileViewController <UIPickerViewDelegate, UIPickerViewDataSource>{
	UIDatePicker		*datePicker;
	NSDate				*initialDate;
	NSInteger			initialExtendedHour;
	NSMutableArray		*aTimes;
	NSInteger			selectedExtendedHour;
	NSString			*currTime;
	id<DateTimePopoverDelegate> __weak _delegate;
	BOOL isDate; // YES for date only (Expense), NO for date time
	NSDate				*dateSelected;
	NSString			*key;
	NSIndexPath			*indexPath;
}

@property (strong, nonatomic) NSDate					*dateSelected;
@property (strong, nonatomic) NSString					*key;
@property BOOL isDate;
@property (strong, nonatomic) IBOutlet UIDatePicker		*datePicker;
@property (strong, nonatomic) NSDate					*initialDate;
@property (nonatomic) NSInteger							initialExtendedHour;
@property (strong, nonatomic) NSMutableArray			*aTimes;
@property (nonatomic) NSInteger							selectedExtendedHour;
@property (strong, nonatomic)NSString					*currTime;
@property (strong, nonatomic)NSIndexPath				*indexPath;

@property (nonatomic, weak) id<DateTimePopoverDelegate> delegate;

-(IBAction) datePickerValueChanged:(id)sender;

-(IBAction) closeDown:(id)sender;

-(IBAction) closeDone:(id)sender;

-(void) onDone;

-(NSDate *)addDaysToDate:(NSDate *)dateDepart NumDaysToAdd:(int)daysToAdd;

-(NSString*) textFromDate:(NSDate*)date;
-(NSDate*) dateFromText:(NSString*)text;

-(void) initDate:(NSDate *) dateCurrent;
-(IBAction)dateDone:(id)sender;
-(IBAction)dateCancel:(id)sender;

@end
